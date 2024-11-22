package org.localStorage.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.localStorage.domain.Note;

public class NoteRepository {
    private final String filePath;
    private final Gson gson = new Gson();
    private Map<Integer, Note> notes = new HashMap<>();
    private int nextId = 1;

    public NoteRepository() {
        this.filePath = "./resources/notes.json";
        loadFromFile();
    }

    public Map<Integer, Note> getNotes() {
        return notes;
    }

    public Note createNote(String title, String body) {
        Note note = new Note(nextId++, title, body);
        notes.put(note.getId(), note);
        saveToFile();
        return note;
    }

    public Note getNoteById(int id) {
        return notes.get(id);
    }

    public boolean updateNote(int id, String title, String body) {
        Note note = notes.get(id);
        if (note != null) {
            note.setTitle(title);
            note.setBody(body);
            saveToFile();
            return true;
        }
        return false;
    }

    public Note replaceNote(int id, String title, String body) {
        Note note = notes.get(id);
        if (note != null) {
            Note newNote = new Note(id, title, body);
            notes.put(id, newNote);
            saveToFile();
            return newNote;
        }
        return null;
    }

    public boolean deleteNoteById(int id) {
        if (notes.remove(id) != null) {
            saveToFile();
            return true;
        }
        return false;
    }

    private void saveToFile() {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(notes, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<Integer, Note>>() {}.getType();
            notes = gson.fromJson(reader, type);
            if (!notes.isEmpty()) {
                nextId = notes.keySet().stream().max(Integer::compare).orElse(0) + 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
