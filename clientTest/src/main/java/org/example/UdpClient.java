package org.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpClient {
    public static void main(String[] args) {
        String serverHost = "localhost";
        int serverPort = 12345;
        String request = "{\"method\": \"PUT\", \"path\": \"/notes/3\", \"body\": {\"title\": \"New Note\"}}";

        try (DatagramSocket socket = new DatagramSocket()) {
            // 서버로 요청 전송
            byte[] buffer = request.getBytes();
            InetAddress serverAddress = InetAddress.getByName(serverHost);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
            socket.send(packet);

            // 서버로부터 응답 수신
            byte[] responseBuffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            socket.receive(responsePacket);

            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            System.out.println("서버 응답: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
