package org.example;

import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class LocalStorageServer {
    private final RequestHandler requestHandler;

    public LocalStorageServer() {
        NoteRepository repository = new NoteRepository();
        this.requestHandler = new RequestHandler(repository);
    }

    public static void main(String[] args) {
        LocalStorageServer server = new LocalStorageServer();
        server.start(3300);
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("LocalStorageServer가 포트 " + port + "에서 실행 중입니다...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    String request = in.readLine();
                    System.out.println("클라이언트 요청: " + request);

                    JSONObject jsonRequest = new JSONObject(request);
                    String method = jsonRequest.getString("method");
                    String path = jsonRequest.getString("path");

                    String response = requestHandler.handleRequest(method, path, jsonRequest);
                    out.println(response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
