package org.udpServer;

import org.udpServer.controller.RequestHandler;

import java.net.*;
import java.sql.*;

public class UdpServer {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mydb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234";

    public static void main(String[] args) {
        int port = 12345;

        try (DatagramSocket serverSocket = new DatagramSocket(port)) {
            System.out.println("UDP 서버가 실행 중입니다...");

            while (true) {
                try {
                    byte[] receiveBuffer = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    serverSocket.receive(receivePacket);
                    String request = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("클라이언트 요청: " + request);

                    // 요청 처리
                    String response;
                    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                        response = RequestHandler.handleRequest(request, connection);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        response = "DB 오류 발생";
                    }

                    // 클라이언트로 응답 전송
                    byte[] sendBuffer = response.getBytes();
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
                    serverSocket.send(sendPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
