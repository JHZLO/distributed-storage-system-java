package org.primaryServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;
import org.primaryServer.controller.RequestHandler;
import org.primaryServer.repository.NoteRepository;
import org.primaryServer.util.Logger;

public class PrimaryStorageServer {
    private final RequestHandler requestHandler;

    private static final List<String> LOCAL_STORAGE_SERVERS = Arrays.asList(
            "http://localhost:3300",
            "http://localhost:3301",
            "http://localhost:3302"
    );

    public PrimaryStorageServer() {
        NoteRepository repository = new NoteRepository();
        this.requestHandler = new RequestHandler(repository);
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("PrimaryStorageServer가 포트 " + port + "에서 실행 중입니다...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    String request = in.readLine();

                    JSONObject jsonRequest = new JSONObject(request);
                    String method = jsonRequest.getString("method");
                    String path = jsonRequest.getString("path");

                    String response = requestHandler.handleRequest(method, path, jsonRequest);

                    // 데이터 변경 요청인 경우 로컬 서버와 동기화
                    if (isDataChangingRequest(method)) {
                        synchronizeToLocalStorageServers(jsonRequest);
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

    private void synchronizeToLocalStorageServers(JSONObject jsonRequest) {
        String originServer = jsonRequest.optString("origin", "");

        for (String serverUrl : LOCAL_STORAGE_SERVERS) {
            String[] serverInfo = serverUrl.replace("http://", "").split(":");
            String host = serverInfo[0];
            int port = Integer.parseInt(serverInfo[1]);

            // 요청을 보낸 서버와 동일한 서버는 동기화 대상에서 제외
            if (serverUrl.equals(originServer)) {
                continue;
            }

            try {
                sendToLocalStorageServer(serverUrl, jsonRequest);
            } catch (IOException e) {
                System.err.println("LocalStorageServer와의 통신 실패: " + serverUrl);
                e.printStackTrace();
            }
        }
    }


    private void sendToLocalStorageServer(String serverUrl, JSONObject jsonRequest) throws IOException {
        jsonRequest.put("origin", "primary");
        String[] serverInfo = serverUrl.replace("http://", "").split(":");
        String host = serverInfo[0];
        int port = Integer.parseInt(serverInfo[1]);

        try (Socket localSocket = new Socket(host, port);
             PrintWriter out = new PrintWriter(localSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(localSocket.getInputStream()))) {

            Logger.log(host + ":" + port, "REQUEST", "Forward Request to LocalStorage");
            out.println(jsonRequest.toString());

            String primaryResponse = in.readLine();
            Logger.log(host + ":" + port, "REPLY", "Acknowledge write completed");

        } catch (IOException e) {
            System.err.println("LocalStorageServer(" + host + ":" + port + ")와의 연결 실패");
            throw e;
        }
    }

    private boolean isDataChangingRequest(String method) { // 데이터 변경 요청인지 확인
        return method.equalsIgnoreCase("POST") ||
                method.equalsIgnoreCase("PUT") ||
                method.equalsIgnoreCase("PATCH") ||
                method.equalsIgnoreCase("DELETE");
    }

    private void sendSuccessResponse(PrintWriter out, String response) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + response.getBytes().length);
        out.println();
        out.println(response);
    }
}
