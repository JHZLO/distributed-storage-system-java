package org.localStorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.JSONArray;
import org.json.JSONObject;
import org.localStorage.controller.RequestHandler;
import org.localStorage.repository.NoteRepository;
import org.localStorage.util.Logger;

public class LocalStorageServer {
    private static final String PRIMARY_SERVER_HOST = "10.20.0.154";
    private static final int PRIMARY_SERVER_PORT = 5001;
    private static final String LOCAL_ADDRESS = "http://10.20.0.121:5000";

    private final RequestHandler requestHandler;

    public LocalStorageServer() {
        NoteRepository repository = new NoteRepository();
        this.requestHandler = new RequestHandler(repository);
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("APILocalStorageServer가 포트 " + port + "에서 실행 중입니다...");
            try {
                initializeFromPrimaryServer(); // PrimaryStorageServer로부터 데이터 초기화
            } catch (Exception e) {
                System.err.println("PrimaryStorageServer로부터 초기화 실패: " + e.getMessage());
            }

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    String firstLine = in.readLine();
                    if (firstLine == null || firstLine.isEmpty()) {
                        sendErrorResponse(out, "요청이 비어 있습니다.");
                        continue;
                    }

                    // HTTP 요청인지 JSON 요청인지 판별
                    if (isHttpRequest(firstLine)) {
                        // HTTP 요청 처리
                        handleHttpRequest(firstLine, in, out);
                    } else {
                        // JSON 요청 처리
                        handleJsonRequest(firstLine, out);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isHttpRequest(String firstLine) {
        return firstLine.startsWith("GET") || firstLine.startsWith("POST") ||
                firstLine.startsWith("PUT") || firstLine.startsWith("DELETE") ||
                firstLine.startsWith("PATCH");
    }

    private void handleHttpRequest(String firstLine, BufferedReader in, PrintWriter out) throws Exception {
        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append(firstLine).append("\n");
        String line;
        int contentLength = 0;

        while ((line = in.readLine()) != null && !line.isEmpty()) {
            headerBuilder.append(line).append("\n");
            if (line.toLowerCase().startsWith("content-length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
        }

        char[] bodyChars = new char[contentLength];
        in.read(bodyChars, 0, contentLength);
        String body = new String(bodyChars);

        JSONObject jsonRequest = parseHttpRequest(headerBuilder.toString(), body);

        handleRequest(jsonRequest, out);
    }

    private JSONObject parseHttpRequest(String header, String body) throws Exception {
        String[] requestParts = header.split(" ");
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("method", requestParts[0]);
        jsonRequest.put("path", requestParts[1]);

        if (!body.isEmpty()) {
            jsonRequest.put("body", new JSONObject(body));
        }

        if (jsonRequest.has("body") && jsonRequest.getJSONObject("body").has("body")) {
            jsonRequest = jsonRequest.getJSONObject("body");
        }
        return jsonRequest;
    }

    private void handleJsonRequest(String firstLine, PrintWriter out) {
        try {
            JSONObject jsonRequest = new JSONObject(firstLine);

            handleRequest(jsonRequest, out);
        } catch (Exception e) {
            sendErrorResponse(out, "JSON 요청 파싱 오류");
        }
    }

    private void handleRequest(JSONObject jsonRequest, PrintWriter out) {
        try {
            String method = jsonRequest.getString("method");
            String path = jsonRequest.getString("path");

            String response = requestHandler.handleRequest(method, path, jsonRequest);

            // 데이터 변경 요청인 경우 Primary로 동기화
            if (isDataChangingRequest(method) && !isFromPrimary(jsonRequest)) {
                syncWithPrimaryServer(jsonRequest);
            }

            if (isFromPrimary(jsonRequest)) {
                out.println(response);
                return;
            }
            sendSuccessResponse(out, response);
        } catch (Exception e) {
            sendErrorResponse(out, "요청 처리 중 오류 발생");
        }
    }

    private void initializeFromPrimaryServer() throws IOException {
        try (Socket primarySocket = new Socket(PRIMARY_SERVER_HOST, PRIMARY_SERVER_PORT);
             PrintWriter out = new PrintWriter(primarySocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(primarySocket.getInputStream()))) {

            JSONObject initRequest = new JSONObject();
            initRequest.put("method", "GET");
            initRequest.put("path", "/init");

            out.println(initRequest.toString());

            String response = in.readLine();
            if (response == null || response.isEmpty()) {
                throw new IOException("PrimaryStorageServer로부터 초기화 응답이 null이거나 비어 있습니다.");
            }

            JSONObject jsonResponse = new JSONObject(response);

            if (jsonResponse.has("notes")) {
                JSONArray notesArray = jsonResponse.getJSONArray("notes");

                requestHandler.clearAllNotes();

                // PrimaryStorageServer의 데이터로 덮어쓰기
                for (int i = 0; i < notesArray.length(); i++) {
                    JSONObject noteJson = notesArray.getJSONObject(i);
                    int id = noteJson.getInt("id");
                    String title = noteJson.optString("title", null);
                    String body = noteJson.optString("body", null);

                    requestHandler.createNoteFromPrimary(id, title, body);
                }

                System.out.println("[INIT FROM PrimaryStorage]");
            } else {
                throw new IOException();
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void syncWithPrimaryServer(JSONObject jsonRequest) {
        try (Socket primarySocket = new Socket(PRIMARY_SERVER_HOST, PRIMARY_SERVER_PORT);
             PrintWriter out = new PrintWriter(primarySocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(primarySocket.getInputStream()))) {

            Logger.log(PRIMARY_SERVER_HOST + ":" + PRIMARY_SERVER_PORT, jsonRequest.toString(),
                    "Forward Request to primary");
            jsonRequest.put("origin", LOCAL_ADDRESS);
            out.println(jsonRequest.toString());

            String primaryResponse = in.readLine();
            Logger.log(PRIMARY_SERVER_HOST + ":" + PRIMARY_SERVER_PORT, primaryResponse, "Acknowledge write completed");

        } catch (IOException e) {
            System.err.println("PrimaryStorageServer와의 동기화 실패");
            e.printStackTrace();
        }
    }

    private boolean isDataChangingRequest(String method) {
        // POST, PUT, PATCH, DELETE 요청만 동기화
        return method.equalsIgnoreCase("POST") ||
                method.equalsIgnoreCase("PUT") ||
                method.equalsIgnoreCase("PATCH") ||
                method.equalsIgnoreCase("DELETE");
    }

    private boolean isFromPrimary(JSONObject jsonRequest) {
        return "primary".equalsIgnoreCase(jsonRequest.optString("origin", ""));
    }

    private void sendErrorResponse(PrintWriter out, String errorMessage) {
        out.println("HTTP/1.1 400 Bad Request");
        out.println("Content-Type: application/json");
        out.println();
        out.println(errorMessage);
    }

    private void sendSuccessResponse(PrintWriter out, String response) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + response.getBytes().length);
        out.println();
        out.println(response);
    }
}
