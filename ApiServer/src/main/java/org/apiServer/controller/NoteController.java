package org.apiServer.controller;

import org.apiServer.domain.Note;
import org.apiServer.service.LocalStorageService;
import org.apiServer.util.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notes")
public class NoteController {

    private final LocalStorageService localStorageService;

    public NoteController(LocalStorageService localStorageService) {
        this.localStorageService = localStorageService;
    }

    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes() {
        String method = "GET";
        String path = "/notes";
        String requestBody = "";
        List<Note> responseBody = localStorageService.getAllNotes();

        Logger.log(method, path, requestBody, responseBody.toString());
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable int id) {
        String method = "GET";
        String path = "/notes/" + id;
        String requestBody = "";

        return localStorageService.getNoteById(id)
                .map(note -> {
                    Logger.log(method, path, requestBody, note.toString());
                    return ResponseEntity.ok(note);
                })
                .orElseGet(() -> {
                    Logger.log(method, path, requestBody, "NOT FOUND");
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping
    public ResponseEntity<Note> createNote(@RequestBody Note note) {
        String method = "POST";
        String path = "/notes";
        String requestBody = note.toString();

        Note createdNote = localStorageService.createNote(note);
        Logger.log(method, path, requestBody, createdNote.toString());
        return ResponseEntity.ok(createdNote);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable int id, @RequestBody Note note) {
        String method = "PUT";
        String path = "/notes/" + id;
        String requestBody = note.toString();

        return localStorageService.updateNote(id, note)
                .map(updatedNote -> {
                    Logger.log(method, path, requestBody, updatedNote.toString());
                    return ResponseEntity.ok(updatedNote);
                })
                .orElseGet(() -> {
                    Logger.log(method, path, requestBody, "NOT FOUND");
                    return ResponseEntity.notFound().build();
                });
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Note> patchNote(@PathVariable int id, @RequestBody Note note) {
        String method = "PATCH";
        String path = "/notes/" + id;
        String requestBody = note.toString();

        return localStorageService.patchNote(id, note)
                .map(patchedNote -> {
                    Logger.log(method, path, requestBody, patchedNote.toString());
                    return ResponseEntity.ok(patchedNote);
                })
                .orElseGet(() -> {
                    Logger.log(method, path, requestBody, "NOT FOUND");
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable int id) {
        String method = "DELETE";
        String path = "/notes/" + id;
        String requestBody = "";

        if (localStorageService.deleteNoteById(id)) {
            String responseBody = "{\"message\": \"메모 삭제 성공\"}";
            Logger.log(method, path, requestBody, responseBody);
            return ResponseEntity.ok(responseBody);
        } else {
            String responseBody = "{\"message\": \"메모 삭제 실패\"}";
            Logger.log(method, path, requestBody, responseBody);
            return ResponseEntity.status(404).body(responseBody);
        }
    }
}
