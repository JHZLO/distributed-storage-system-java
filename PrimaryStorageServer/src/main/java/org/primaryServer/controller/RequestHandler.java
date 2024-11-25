package org.primaryServer.controller;

import com.google.gson.Gson;
import org.json.JSONObject;
import org.primaryServer.domain.Note;
import org.primaryServer.dto.ErrorResponse;
import org.primaryServer.dto.SuccessResponse;
import org.primaryServer.repository.NoteRepository;

public class RequestHandler {
    private final NoteRepository repository;
    private final Gson gson = new Gson();

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
        if (method.equals("PATCH") && path.matches("/notes/\\d+")) {
            return handlePatchNote(path, jsonRequest);
        }
        if (method.equals("DELETE") && path.matches("/notes/\\d+")) {
            return handleDeleteNote(path);
        }
        return gson.toJson(new ErrorResponse("잘못된 요청"));
    }

    public String handleGetAllNotes() {
        return gson.toJson(repository.getNotes().values());
    }

    private String handleGetNoteById(String path) {
        int id = Integer.parseInt(path.split("/")[2]);
        Note note = repository.getNoteById(id);
        if (note != null) {
            return gson.toJson(note);
        }
        return gson.toJson(new ErrorResponse("메모가 존재하지 않습니다."));
    }

    private String handlePostNote(JSONObject jsonRequest) {
        String title = jsonRequest.getJSONObject("body").getString("title");
        String body = jsonRequest.getJSONObject("body").getString("body");
        Note note = repository.createNote(title, body);
        return gson.toJson(note);
    }

    private String handlePutNote(String path, JSONObject jsonRequest) {
        int id = Integer.parseInt(path.split("/")[2]);

        Note note = repository.getNoteById(id);
        if (note == null) {
            return gson.toJson(new ErrorResponse("메모가 존재하지 않습니다."));
        }

        String title = jsonRequest.getJSONObject("body").optString("title", null);
        String body = jsonRequest.getJSONObject("body").optString("body", null);

        Note updatedNote = repository.replaceNote(id, title, body);
        if (updatedNote != null) {
            return gson.toJson(updatedNote);
        }

        return gson.toJson(new ErrorResponse("메모 업데이트 실패"));
    }


    private String handlePatchNote(String path, JSONObject jsonRequest) {
        int id = Integer.parseInt(path.split("/")[2]);
        Note note = repository.getNoteById(id);

        if (note == null) {
            return gson.toJson(new ErrorResponse("메모가 존재하지 않습니다."));
        }

        String title = jsonRequest.getJSONObject("body").optString("title", note.getTitle());
        String body = jsonRequest.getJSONObject("body").optString("body", note.getBody());

        boolean updated = repository.updateNote(id, title, body);
        if (updated) {
            return gson.toJson(upda)
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
