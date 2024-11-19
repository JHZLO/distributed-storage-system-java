package org.TcpServer;

import java.io.*;
import java.net.*;

public class TcpServer {
    public static void main(String[] args) {
        String storageHost = "localhost"; // localStorageServer
        int storagePort = 3300;

        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("TCP 서버가 실행 중입니다...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    String clientRequest = in.readLine();
                    System.out.println("클라이언트 요청: " + clientRequest);

                    try (Socket storageSocket = new Socket(storageHost, storagePort);
                         PrintWriter storageOut = new PrintWriter(storageSocket.getOutputStream(), true);
                         BufferedReader storageIn = new BufferedReader(new InputStreamReader(storageSocket.getInputStream()))) {

                        storageOut.println(clientRequest);
                        String storageResponse = storageIn.readLine();
                        out.println(storageResponse);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
