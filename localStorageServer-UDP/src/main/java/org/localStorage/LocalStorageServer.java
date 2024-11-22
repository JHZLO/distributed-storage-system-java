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
    private static final String LOCAL_ADDRESS = "http://10.20.0.119:5000";

    private final RequestHandler requestHandler;

    public LocalStorageServer() {
        NoteRepository repository = new NoteRepository();
        this.requestHandler = new RequestHandler(repository);
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("LocalStorageServer가 포트 " + port + "에서 실행 중입니다...");
            try {
                initializeFromPrimaryServer(); // PrimaryStorageServer로부터 데이터 초기화
            } catch (Exception e) {
                System.err.println("PrimaryStorageServer로부터 초기화 실패: " + e.getMessage());
            }

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    String request = in.readLine();

                    JSONObject jsonRequest = new JSONObject(request);
                    String method = jsonRequest.getString("method");
                    String path = jsonRequest.getString("path");

                    String response = requestHandler.handleRequest(method, path, jsonRequest);

                    // 데이터 변화가 있는 요청인 경우 PrimaryStorageServer와 동기화
                    if (isDataChangingRequest(method) && !isFromPrimary(jsonRequest)) {
                        syncWithPrimaryServer(jsonRequest);
                    }

                    out.println(response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                    String title = noteJson.getString("title");
                    String body = noteJson.getString("body");

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

            jsonRequest.put("origin", LOCAL_ADDRESS);

            Logger.log(PRIMARY_SERVER_HOST + ":" + PRIMARY_SERVER_PORT, "REQUEST", "Forward Request to primary");
            out.println(jsonRequest.toString());

            String primaryResponse = in.readLine();
            Logger.log(PRIMARY_SERVER_HOST + ":" + PRIMARY_SERVER_PORT, "REPLY", "Acknowledge write completed");

        } catch (IOException e) {
            System.err.println("PrimaryStorageServer와의 동기화 실패");
            e.printStackTrace();
        }
    }

    private boolean isDataChangingRequest(String method) { // 데이터 변화가 있는 경우
        // POST, PUT, PATCH, DELETE 요청만 동기화
        return method.equalsIgnoreCase("POST") ||
                method.equalsIgnoreCase("PUT") ||
                method.equalsIgnoreCase("PATCH") ||
                method.equalsIgnoreCase("DELETE");
    }

    private boolean isFromPrimary(JSONObject jsonRequest) { // 요청이 primary에서 왔는지 확인
        return "primary".equalsIgnoreCase(jsonRequest.optString("origin", ""));
    }
}
