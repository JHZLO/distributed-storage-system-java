package org.udpServer;

import java.io.*;
import java.net.*;

public class UdpServer {
    public static void main(String[] args) {
        String storageHost = "localhost";
        int storagePort = 3302;
        int udpServerPort = 12345;

        try (DatagramSocket udpSocket = new DatagramSocket(udpServerPort)) {
            System.out.println("UDP 서버가 실행 중입니다...");

            while (true) {
                try {
                    byte[] receiveBuffer = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    udpSocket.receive(receivePacket);
                    String clientRequest = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("클라이언트 요청: " + clientRequest);

                    String storageResponse;
                    try (Socket storageSocket = new Socket(storageHost, storagePort);
                         PrintWriter storageOut = new PrintWriter(storageSocket.getOutputStream(), true);
                         BufferedReader storageIn = new BufferedReader(new InputStreamReader(storageSocket.getInputStream()))) {

                        storageOut.println(clientRequest);
                        storageResponse = storageIn.readLine();
                    }

                    byte[] sendBuffer = storageResponse.getBytes();
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
                    udpSocket.send(sendPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
