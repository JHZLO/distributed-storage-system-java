package org.localStorage;

import org.json.JSONObject;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import org.localStorage.controller.RequestHandler;
import org.localStorage.repository.NoteRepository;

public class LocalStorageServer {
    private final RequestHandler requestHandler;

    private static final String PRIMARY_SERVER_HOST = "localhost";
    private static final int PRIMARY_SERVER_PORT = 5001;

    public LocalStorageServer() {
        NoteRepository repository = new NoteRepository();
        this.requestHandler = new RequestHandler(repository);
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("APILocalStorageServer가 포트 " + port + "에서 실행 중입니다...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    StringBuilder requestBuilder = new StringBuilder();
                    String line;
                    int contentLength = 0;

                    // HTTP Header 읽기
                    while ((line = in.readLine()) != null && !line.isEmpty()) {
                        requestBuilder.append(line).append("\n");

                        if (line.toLowerCase().startsWith("content-length:")) {
                            contentLength = Integer.parseInt(line.split(":")[1].trim());
                        }
                    }

                    // HTTP Body 읽기
                    char[] bodyChars = new char[contentLength];
                    in.read(bodyChars, 0, contentLength);
                    String body = new String(bodyChars);

                    // JSON 요청 생성
                    JSONObject jsonRequest = new JSONObject();
                    String[] requestParts = requestBuilder.toString().split(" ");
                    jsonRequest.put("method", requestParts[0]);
                    jsonRequest.put("path", requestParts[1]);

                    if (!body.isEmpty()) {
                        try {
                            jsonRequest.put("body", new JSONObject(body));
                        } catch (Exception e) {
                            sendErrorResponse(out, "본문을 JSON으로 파싱할 수 없습니다.");
                            continue;
                        }
                    }

                    // 중첩된 body 구조 처리
                    if (jsonRequest.has("body") && jsonRequest.getJSONObject("body").has("body")) {
                        jsonRequest = jsonRequest.getJSONObject("body");
                    }

                    System.out.println("클라이언트 요청: " + jsonRequest);

                    // 요청 처리
                    String method = jsonRequest.getString("method");
                    String path = jsonRequest.getString("path");

                    String response = requestHandler.handleRequest(method, path, jsonRequest);

                    // 데이터 변경 요청이면서 Primary가 아닌 경우 동기화
                    if (isDataChangingRequest(method) && !isFromPrimary(jsonRequest)) {
                        syncWithPrimaryServer(jsonRequest);
                    }

                    sendSuccessResponse(out, response);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void syncWithPrimaryServer(JSONObject jsonRequest) {
        try (Socket primarySocket = new Socket(PRIMARY_SERVER_HOST, PRIMARY_SERVER_PORT);
             PrintWriter out = new PrintWriter(primarySocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(primarySocket.getInputStream()))) {

            System.out.println("PrimaryStorageServer에 동기화 요청: " + jsonRequest);
            out.println(jsonRequest.toString());

            String primaryResponse = in.readLine();
            System.out.println("PrimaryStorageServer 응답: " + primaryResponse);

        } catch (IOException e) {
            System.err.println("PrimaryStorageServer와의 동기화 실패");
            e.printStackTrace();
        }
    }

    /**
     * 데이터 변화가 있는 요청인지 확인
     */
    private boolean isDataChangingRequest(String method) {
        // POST, PUT, PATCH, DELETE 요청만 동기화
        return method.equalsIgnoreCase("POST") ||
                method.equalsIgnoreCase("PUT") ||
                method.equalsIgnoreCase("PATCH") ||
                method.equalsIgnoreCase("DELETE");
    }

    /**
     * 요청이 PrimaryStorageServer로부터 온 것인지 확인
     */
    private boolean isFromPrimary(JSONObject jsonRequest) {
        return "primary".equalsIgnoreCase(jsonRequest.optString("origin", ""));
    }

    private void sendErrorResponse(PrintWriter out, String errorMessage) {
        out.println("HTTP/1.1 400 Bad Request");
        out.println("Content-Type: application/json");
        out.println();
        out.println("{\"error\": \"" + errorMessage + "\"}");
    }

    private void sendSuccessResponse(PrintWriter out, String response) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + response.getBytes().length);
        out.println();
        out.println(response);
    }
}
