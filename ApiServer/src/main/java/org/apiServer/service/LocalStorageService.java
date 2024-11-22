package org.apiServer.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apiServer.domain.Note;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LocalStorageService {

    private final RestTemplate restTemplate;

    @Value("${local-storage.url}")
    private String localStorageUrl;

    public LocalStorageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Note> getAllNotes() {
        return Arrays.asList(restTemplate.getForObject(localStorageUrl + "/notes", Note[].class));
    }

    public Optional<Note> getNoteById(int id) {
        try {
            Note note = restTemplate.getForObject(localStorageUrl + "/notes/" + id, Note.class);
            return Optional.ofNullable(note);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Note createNote(Note note) {
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("method", "POST");
        jsonRequest.put("path", "/notes");

        JSONObject body = new JSONObject();
        body.put("title", note.getTitle());
        body.put("body", note.getBody());
        jsonRequest.put("body", body);

        return restTemplate.postForObject(localStorageUrl, jsonRequest.toString(), Note.class);
    }

    public Optional<Note> updateNote(int id, Note note) {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("method", "PUT");
            jsonRequest.put("path", "/notes/" + id);

            JSONObject body = new JSONObject();
            body.put("title", note.getTitle());
            body.put("body", note.getBody());
            jsonRequest.put("body", body);

            restTemplate.postForObject(localStorageUrl, jsonRequest.toString(), Void.class);

            note.setId(id);
            return Optional.of(note);
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    public Optional<Note> patchNote(int id, Note partialNote) {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("method", "PATCH");
            jsonRequest.put("path", "/notes/" + id);

            JSONObject body = new JSONObject();
            if (partialNote.getTitle() != null) {
                body.put("title", partialNote.getTitle());
            }
            if (partialNote.getBody() != null) {
                body.put("body", partialNote.getBody());
            }
            jsonRequest.put("body", body);

            restTemplate.postForObject(localStorageUrl, jsonRequest.toString(), Void.class);
            return getNoteById(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean deleteNoteById(int id) {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("method", "DELETE");
            jsonRequest.put("path", "/notes/" + id);

            restTemplate.postForObject(localStorageUrl, jsonRequest.toString(), Void.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
