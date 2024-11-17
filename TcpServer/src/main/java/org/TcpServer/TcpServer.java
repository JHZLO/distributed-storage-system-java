package org.TcpServer;

import org.TcpServer.controller.RequestHandler;

import java.io.*;
import java.net.*;
import java.sql.*;

public class TcpServer {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mydb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("TCP 서버가 실행 중입니다...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    // 요청 읽기
                    String request = in.readLine();
                    System.out.println("클라이언트 요청: " + request);

                    // 데이터베이스 연결
                    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                        String response = RequestHandler.handleRequest(request, connection);
                        out.println(response);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        out.println("DB 오류 발생");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
