package org.localStorage;

import org.json.JSONObject;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import org.localStorage.controller.RequestHandler;
import org.localStorage.repository.NoteRepository;

public class LocalStorageServer {
    private final RequestHandler requestHandler;

    public LocalStorageServer() {
        NoteRepository repository = new NoteRepository();
        this.requestHandler = new RequestHandler(repository);
    }

    public static void main(String[] args) {
        LocalStorageServer server = new LocalStorageServer();
        server.start(3301); // API 서버와 다른 포트를 사용
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("LocalStorageServer가 포트 " + port + "에서 실행 중입니다...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    // HTTP 요청 읽기
                    StringBuilder requestBuilder = new StringBuilder();
                    String line;
                    int contentLength = 0;

                    while ((line = in.readLine()) != null && !line.isEmpty()) {
                        requestBuilder.append(line).append("\n");

                        // Content-Length 헤더를 읽어 본문 길이를 가져옴
                        if (line.toLowerCase().startsWith("content-length:")) {
                            contentLength = Integer.parseInt(line.split(":")[1].trim());
                        }
                    }

                    // 요청 본문 읽기
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
                    String response = requestHandler.handleRequest(
                            jsonRequest.getString("method"),
                            jsonRequest.getString("path"),
                            jsonRequest
                    );

                    // 성공 응답
                    sendSuccessResponse(out, response);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
