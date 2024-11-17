package org.TcpServer.repository;

import org.json.JSONObject;

import java.sql.*;

public class MemoRepository {

    public static String getAllNotes(Connection connection) throws SQLException {
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

    public static String getNoteById(Connection connection, int id) throws SQLException {
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

    public static String createNote(Connection connection, JSONObject body) throws SQLException {
        String query = "INSERT INTO memo (title, body) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            String title = body.getString("title");
            String content = body.getString("body");
            statement.setString(1, title);
            statement.setString(2, content);
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0 ? "메모 생성 성공" : "메모 생성 실패";
        }
    }

    public static String updateNote(Connection connection, int id, JSONObject body) throws SQLException {
        String query = "UPDATE memo SET title = ?, body = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            String title = body.getString("title");
            String content = body.getString("body");
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setInt(3, id);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0 ? "메모 업데이트 성공" : "메모 업데이트 실패";
        }
    }

    public static String patchNote(Connection connection, int id, JSONObject body) throws SQLException {
        String query = "UPDATE memo SET title = COALESCE(?, title), body = COALESCE(?, body) WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            String title = body.optString("title", null);
            String content = body.optString("body", null);
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setInt(3, id);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0 ? "메모 일부 업데이트 성공" : "메모 업데이트 실패";
        }
    }

    public static String deleteNoteById(Connection connection, int id) throws SQLException {
        String query = "DELETE FROM memo WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0 ? "메모 삭제 성공" : "메모 삭제 실패";
        }
    }
}
