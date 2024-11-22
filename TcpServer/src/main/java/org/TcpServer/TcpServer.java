package org.TcpServer;

import org.TcpServer.util.Logger;
import org.json.JSONObject;

import java.io.*;
import java.net.*;

public class TcpServer {
    public static void main(String[] args) {
        String storageHost = "localhost";
        int storagePort = 3300;

        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("TCP 서버가 실행 중입니다...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    String clientRequest = in.readLine();

                    String method = "UNKNOWN";
                    String path = "UNKNOWN";
                    String requestBody = clientRequest;
                    String responseBody = "";

                    try {
                        if (clientRequest != null) {
                            JSONObject jsonRequest = new JSONObject(clientRequest);
                            method = jsonRequest.optString("method", "UNKNOWN");
                            path = jsonRequest.optString("path", "UNKNOWN");
                        }
                    } catch (Exception e) {
                        System.err.println("요청 JSON 파싱 중 오류 발생: " + e.getMessage());
                    }

                    try (Socket storageSocket = new Socket(storageHost, storagePort);
                         PrintWriter storageOut = new PrintWriter(storageSocket.getOutputStream(), true);
                         BufferedReader storageIn = new BufferedReader(new InputStreamReader(storageSocket.getInputStream()))) {

                        storageOut.println(clientRequest);

                        responseBody = storageIn.readLine();
                        out.println(responseBody);
                    }

                    Logger.log(method, path, requestBody, responseBody);

                } catch (IOException e) {
                    System.err.println("클라이언트 요청 처리 중 오류 발생");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("TCP 서버 실행 중 오류 발생");
            e.printStackTrace();
        }
    }
}
