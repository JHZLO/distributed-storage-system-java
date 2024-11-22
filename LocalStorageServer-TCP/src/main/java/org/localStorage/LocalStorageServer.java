package org.localStorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.JSONObject;
import org.localStorage.controller.RequestHandler;
import org.localStorage.repository.NoteRepository;
import org.localStorage.util.Logger;

public class LocalStorageServer {
    private static final String PRIMARY_SERVER_HOST = "10.20.0.154";
    private static final int PRIMARY_SERVER_PORT = 5001;
    private static final String LOCAL_ADDRESS = "http://10.20.0.161:5000";

    private final RequestHandler requestHandler;

    public LocalStorageServer() {
        NoteRepository repository = new NoteRepository();
        this.requestHandler = new RequestHandler(repository);
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("LocalStorageServer가 포트 " + port + "에서 실행 중입니다...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    String request = in.readLine();

                    JSONObject jsonRequest = new JSONObject(request);
                    String method = jsonRequest.getString("method");
                    String path = jsonRequest.getString("path");

                    String response = requestHandler.handleRequest(method, path, jsonRequest);

                    if (isDataChangingRequest(method) && !isFromPrimary(jsonRequest)) {
                        syncWithPrimaryServer(method, path, jsonRequest);
                    }

                    out.println(response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void syncWithPrimaryServer(String method, String path, JSONObject jsonRequest) {
        try (Socket primarySocket = new Socket(PRIMARY_SERVER_HOST, PRIMARY_SERVER_PORT);
             PrintWriter out = new PrintWriter(primarySocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(primarySocket.getInputStream()))) {

            jsonRequest.put("origin",LOCAL_ADDRESS);

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
