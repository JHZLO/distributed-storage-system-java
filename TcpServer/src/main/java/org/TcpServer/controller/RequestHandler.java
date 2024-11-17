package org.TcpServer.controller;
import org.TcpServer.repository.MemoRepository;
import org.json.JSONObject;

import java.sql.Connection;

public class RequestHandler {

    public static String handleRequest(String request, Connection connection) {
        try {
            // JSON 파싱
            JSONObject jsonRequest = new JSONObject(request);
            String method = jsonRequest.getString("method");
            String path = jsonRequest.getString("path");

            // 요청 처리 로직
            switch (method) {
                case "GET" -> {
                    if (path.equals("/notes")) {
                        return MemoRepository.getAllNotes(connection);
                    } else if (path.matches("/notes/\\d+")) {
                        int id = Integer.parseInt(path.split("/")[2]);
                        return MemoRepository.getNoteById(connection, id);
                    }
                }
                case "POST" -> {
                    if (path.equals("/notes")) {
                        return MemoRepository.createNote(connection, jsonRequest.getJSONObject("body"));
                    }
                }
                case "PUT" -> {
                    if (path.matches("/notes/\\d+")) {
                        int id = Integer.parseInt(path.split("/")[2]);
                        return MemoRepository.updateNote(connection, id, jsonRequest.getJSONObject("body"));
                    }
                }
                case "PATCH" -> {
                    if (path.matches("/notes/\\d+")) {
                        int id = Integer.parseInt(path.split("/")[2]);
                        return MemoRepository.patchNote(connection, id, jsonRequest.getJSONObject("body"));
                    }
                }
                case "DELETE" -> {
                    if (path.matches("/notes/\\d+")) {
                        int id = Integer.parseInt(path.split("/")[2]);
                        return MemoRepository.deleteNoteById(connection, id);
                    }
                }
                default -> {
                    return "잘못된 요청";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "요청 처리 중 오류 발생";
        }
        return "잘못된 요청";
    }
}
