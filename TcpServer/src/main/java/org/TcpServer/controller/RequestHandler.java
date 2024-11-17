package org.TcpServer.controller;

import org.TcpServer.database.MemoRepository;
import org.json.JSONObject;

import java.sql.Connection;

public class RequestHandler {

    public static String handleRequest(String request, Connection connection) {
        String response = "잘못된 요청"; // 기본 응답값

        try {
            // JSON 파싱
            JSONObject jsonRequest = new JSONObject(request);
            String method = jsonRequest.getString("method");
            String path = jsonRequest.getString("path");

            // 요청 처리
            if (method.equalsIgnoreCase("GET") && path.equals("/notes")) {
                response = MemoRepository.getAllNotes(connection);
            }

            if (method.equalsIgnoreCase("GET") && path.matches("/notes/\\d+")) {
                int id = Integer.parseInt(path.split("/")[2]);
                response = MemoRepository.getNoteById(connection, id);
            }

            if (method.equalsIgnoreCase("POST") && path.equals("/notes")) {
                response = MemoRepository.createNote(connection, jsonRequest.getJSONObject("body"));
            }

            if (method.equalsIgnoreCase("PUT") && path.matches("/notes/\\d+")) {
                int id = Integer.parseInt(path.split("/")[2]);
                response = MemoRepository.updateNote(connection, id, jsonRequest.getJSONObject("body"));
            }

            if (method.equalsIgnoreCase("PATCH") && path.matches("/notes/\\d+")) {
                int id = Integer.parseInt(path.split("/")[2]);
                response = MemoRepository.patchNote(connection, id, jsonRequest.getJSONObject("body"));
            }

            if (method.equalsIgnoreCase("DELETE") && path.matches("/notes/\\d+")) {
                int id = Integer.parseInt(path.split("/")[2]);
                response = MemoRepository.deleteNoteById(connection, id);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response = "요청 처리 중 오류 발생";
        }

        return response;
    }
}
