package org.example;

import java.net.*;
import java.sql.*;
import org.json.JSONObject;

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
                    // 클라이언트로부터 데이터 수신
                    byte[] receiveBuffer = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    serverSocket.receive(receivePacket);
                    String request = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("클라이언트 요청: " + request);

                    // JSON 요청 파싱
                    JSONObject jsonRequest = new JSONObject(request);
                    String method = jsonRequest.getString("method");
                    String path = jsonRequest.getString("path");

                    // 데이터베이스 처리 및 응답 생성
                    String response;
                    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                        response = handleRequest(method, path, jsonRequest, connection);
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

    private static String handleRequest(String method, String path, JSONObject jsonRequest, Connection connection) throws SQLException {
        String response = "";

        if (method.equals("GET") && path.equals("/notes")) {
            response = getAllNotes(connection);
        } else if (method.equals("GET") && path.matches("/notes/\\d+")) {
            int id = Integer.parseInt(path.split("/")[2]);
            response = getNoteById(connection, id);
        } else if (method.equals("POST") && path.equals("/notes")) {
            response = createNote(connection, jsonRequest);
        } else if (method.equals("PUT") && path.matches("/notes/\\d+")) {
            int id = Integer.parseInt(path.split("/")[2]);
            response = updateNote(connection, id, jsonRequest);
        } else if (method.equals("PATCH") && path.matches("/notes/\\d+")) {
            int id = Integer.parseInt(path.split("/")[2]);
            response = patchNote(connection, id, jsonRequest);
        } else if (method.equals("DELETE") && path.matches("/notes/\\d+")) {
            int id = Integer.parseInt(path.split("/")[2]);
            response = deleteNoteById(connection, id);
        } else {
            response = "잘못된 요청";
        }
        return response;
    }

    private static String getAllNotes(Connection connection) throws SQLException {
        StringBuilder result = new StringBuilder();
        String query = "SELECT id, title, body FROM memo";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                result.append("ID: ").append(resultSet.getInt("id")).append(", ");
                result.append("Title: ").append(resultSet.getString("title")).append(", ");
                result.append("Body: ").append(resultSet.getString("body")).append("\n");
            }
        }
        return result.toString();
    }

    private static String getNoteById(Connection connection, int id) throws SQLException {
        String query = "SELECT id, title, body FROM memo WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return "ID: " + resultSet.getInt("id") + ", Title: " + resultSet.getString("title") + ", Body: " + resultSet.getString("body");
                } else {
                    return "메모가 존재하지 않습니다.";
                }
            }
        }
    }

    private static String createNote(Connection connection, JSONObject jsonRequest) throws SQLException {
        String query = "INSERT INTO memo (title, body) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            String title = jsonRequest.getJSONObject("body").getString("title");
            String body = jsonRequest.getJSONObject("body").getString("body");
            statement.setString(1, title);
            statement.setString(2, body);
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0 ? "메모 생성 성공" : "메모 생성 실패";
        }
    }

    private static String updateNote(Connection connection, int id, JSONObject jsonRequest) throws SQLException {
        String query = "UPDATE memo SET title = ?, body = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            String title = jsonRequest.getJSONObject("body").getString("title");
            String body = jsonRequest.getJSONObject("body").getString("body");
            statement.setString(1, title);
            statement.setString(2, body);
            statement.setInt(3, id);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0 ? "메모 업데이트 성공" : "메모 업데이트 실패";
        }
    }

    private static String patchNote(Connection connection, int id, JSONObject jsonRequest) throws SQLException {
        String query = "UPDATE memo SET title = COALESCE(?, title), body = COALESCE(?, body) WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            String title = jsonRequest.getJSONObject("body").optString("title", null);
            String body = jsonRequest.getJSONObject("body").optString("body", null);
            statement.setString(1, title);
            statement.setString(2, body);
            statement.setInt(3, id);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0 ? "메모 일부 업데이트 성공" : "메모 업데이트 실패";
        }
    }

    private static String deleteNoteById(Connection connection, int id) throws SQLException {
        String query = "DELETE FROM memo WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0 ? "메모 삭제 성공" : "메모 삭제 실패";
        }
    }
}
