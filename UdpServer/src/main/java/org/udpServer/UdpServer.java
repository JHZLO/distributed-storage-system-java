package org.udpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import org.json.JSONObject;
import org.udpServer.util.Logger;

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

                    String method = "UNKNOWN";
                    String path = "UNKNOWN";
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
                         BufferedReader storageIn = new BufferedReader(
                                 new InputStreamReader(storageSocket.getInputStream()))) {

                        storageOut.println(clientRequest);
                        responseBody = storageIn.readLine();
                    }

                    byte[] sendBuffer = responseBody.getBytes();
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress,
                            clientPort);
                    udpSocket.send(sendPacket);

                    Logger.log(method, path, clientRequest, responseBody);

                } catch (Exception e) {
                    System.err.println("요청 처리 중 오류 발생");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("UDP 서버 실행 중 오류 발생");
            e.printStackTrace();
        }
    }
}
