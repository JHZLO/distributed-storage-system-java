package org.tcpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpClient {
    public static void main(String[] args) {
        String serverHost = "10.20.0.161"; // 서버 호스트
        int serverPort = 12345; // 서버 포트
        String request = "{\"method\": \"POST\", \"path\": \"/notes\", \"body\": {\"title\": \"TCP Note\", \"body\": \"This is a TCP note\"}}";

        try (Socket socket = new Socket(serverHost, serverPort);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(request);
            System.out.println("TCP 서버로 요청: " + request);

            String response = in.readLine();
            System.out.println("서버 응답: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
