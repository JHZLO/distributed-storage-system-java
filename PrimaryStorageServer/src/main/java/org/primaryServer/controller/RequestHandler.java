package org.primaryServer.controller;

import com.google.gson.Gson;
import org.json.JSONObject;
import org.primaryServer.domain.Note;
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
        return createErrorResponse();
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
        return createErrorResponse();
    }

    private String handlePostNote(JSONObject jsonRequest) {
        try {
            String title = jsonRequest.getJSONObject("body").getString("title");
            String body = jsonRequest.getJSONObject("body").getString("body");
            Note note = repository.createNote(title, body);

            JSONObject response = new JSONObject();
            response.put("id", note.getId());
            response.put("title", note.getTitle());
            response.put("body", note.getBody());
            return response.toString();
        } catch (Exception e) {
            return createErrorResponse();
        }
    }

    private String handlePutNote(String path, JSONObject jsonRequest) {
        int id = Integer.parseInt(path.split("/")[2]);
        Note note = repository.getNoteById(id);
        if (note == null) {
            return createErrorResponse();
        }

        String title = jsonRequest.getJSONObject("body").optString("title", null);
        String body = jsonRequest.getJSONObject("body").optString("body", null);
        Note updatedNote = repository.replaceNote(id, title, body);

        if (updatedNote != null) {
            return gson.toJson(updatedNote);
        }
        return createErrorResponse();
    }

    private String handlePatchNote(String path, JSONObject jsonRequest) {
        int id = Integer.parseInt(path.split("/")[2]);
        Note note = repository.getNoteById(id);
        if (note == null) {
            return createErrorResponse();
        }

        String title = jsonRequest.getJSONObject("body").optString("title", note.getTitle());
        String body = jsonRequest.getJSONObject("body").optString("body", note.getBody());
        boolean updated = repository.updateNote(id, title, body);

        if (updated) {
            JSONObject response = new JSONObject();
            response.put("id", id);
            response.put("title", title);
            response.put("body", body);
            return response.toString();
        }
        return createErrorResponse(); // 에러 메시지 통일
    }

    private String handleDeleteNote(String path) {
        int id = Integer.parseInt(path.split("/")[2]);
        boolean deleted = repository.deleteNoteById(id);

        if (deleted) {
            JSONObject response = new JSONObject();
            response.put("msg", "OK");
            return response.toString();
        }
        return createErrorResponse();
    }

    private String createErrorResponse() {
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("msg", "ERROR");
        return errorResponse.toString();
    }
}
