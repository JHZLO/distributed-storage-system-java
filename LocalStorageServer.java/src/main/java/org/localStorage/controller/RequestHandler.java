package org.localStorage.controller;

import org.localStorage.domain.Note;
import org.localStorage.dto.ErrorResponse;
import org.localStorage.dto.SuccessResponse;
import org.json.JSONObject;
import com.google.gson.Gson;
import org.localStorage.repository.NoteRepository;

public class RequestHandler {
    private final NoteRepository repository;
    private final Gson gson = new Gson(); // Gson 객체 추가

    public RequestHandler(NoteRepository repository) {
        this.repository = repository;
    }

    public String handleRequest(String method, String path, JSONObject jsonRequest) {
        if (method.equals("GET") && path.equals("/notes")) {
            return handleGetAllNotes();
        }
        if (method.equals("GET") && path.matches("/notes/\\d+")) {
            return handleGetNoteById(path);
        }
        if (method.equals("POST") && path.equals("/notes")) {
            return handlePostNote(jsonRequest);
        }
        if (method.equals("PUT") && path.matches("/notes/\\d+")) {
            return handlePutNote(path, jsonRequest);
        }
        if (method.equals("DELETE") && path.matches("/notes/\\d+")) {
            return handleDeleteNote(path);
        }
        return gson.toJson(new ErrorResponse("잘못된 요청")); // JSON 형식으로 에러 반환
    }

    private String handleGetAllNotes() {
        return gson.toJson(repository.getNotes().values()); // JSON 배열 반환
    }

    private String handleGetNoteById(String path) {
        int id = Integer.parseInt(path.split("/")[2]);
        Note note = repository.getNoteById(id);
        if (note != null) {
            return gson.toJson(note); // Note 객체를 JSON으로 반환
        }
        return gson.toJson(new ErrorResponse("메모가 존재하지 않습니다.")); // 에러 메시지를 JSON으로 반환
    }

    private String handlePostNote(JSONObject jsonRequest) {
        String title = jsonRequest.getJSONObject("body").getString("title");
        String body = jsonRequest.getJSONObject("body").getString("body");
        Note note = repository.createNote(title, body);
        return gson.toJson(note); // 새로 생성된 Note 객체를 JSON으로 반환
    }

    private String handlePutNote(String path, JSONObject jsonRequest) {
        int id = Integer.parseInt(path.split("/")[2]);
        String title = jsonRequest.getJSONObject("body").getString("title");
        String body = jsonRequest.getJSONObject("body").getString("body");
        boolean updated = repository.updateNote(id, title, body);
        if (updated) {
            return gson.toJson(new SuccessResponse("메모 업데이트 성공"));
        }
        return gson.toJson(new ErrorResponse("메모 업데이트 실패"));
    }

    private String handleDeleteNote(String path) {
        int id = Integer.parseInt(path.split("/")[2]);
        boolean deleted = repository.deleteNoteById(id);
        if (deleted) {
            return gson.toJson(new SuccessResponse("메모 삭제 성공"));
        }
        return gson.toJson(new ErrorResponse("메모 삭제 실패"));
    }
}
