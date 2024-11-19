package org.example.controller;

import org.example.domain.Note;
import org.example.service.LocalStorageService;
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
        return ResponseEntity.ok(localStorageService.getAllNotes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable int id) {
        return localStorageService.getNoteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Note> createNote(@RequestBody Note note) {
        return ResponseEntity.ok(localStorageService.createNote(note));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable int id, @RequestBody Note note) {
        return localStorageService.updateNote(id, note)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Note> patchNote(@PathVariable int id, @RequestBody Note note) {
        return localStorageService.patchNote(id, note)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable int id) {
        if (localStorageService.deleteNoteById(id)) {
            return ResponseEntity.ok("{\"message\": \"메모 삭제 성공\"}");
        }
        return ResponseEntity.status(404).body("{\"message\": \"메모 삭제 실패\"}");
    }
}
