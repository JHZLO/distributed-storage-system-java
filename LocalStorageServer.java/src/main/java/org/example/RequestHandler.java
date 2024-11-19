package org.example;

import org.json.JSONObject;

public class RequestHandler {
    private final NoteRepository repository;

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
        return "잘못된 요청";
    }


    private String handleGetAllNotes() {
        return repository.getNotes().values().toString();
    }

    private String handleGetNoteById(String path) {
        int id = Integer.parseInt(path.split("/")[2]);
        Note note = repository.getNoteById(id);
        return note != null ? note.toString() : "메모가 존재하지 않습니다.";
    }

    private String handlePostNote(JSONObject jsonRequest) {
        String title = jsonRequest.getJSONObject("body").getString("title");
        String body = jsonRequest.getJSONObject("body").getString("body");
        Note note = repository.createNote(title, body);
        return note.toString();
    }

    private String handlePutNote(String path, JSONObject jsonRequest) {
        int id = Integer.parseInt(path.split("/")[2]);
        String title = jsonRequest.getJSONObject("body").getString("title");
        String body = jsonRequest.getJSONObject("body").getString("body");
        return repository.updateNote(id, title, body) ? "메모 업데이트 성공" : "메모 업데이트 실패";
    }

    private String handleDeleteNote(String path) {
        int id = Integer.parseInt(path.split("/")[2]);
        return repository.deleteNoteById(id) ? "메모 삭제 성공" : "메모 삭제 실패";
    }
}
