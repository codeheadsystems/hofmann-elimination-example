package com.codeheadsystems.hofmann.example.notes;

import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class InMemoryNoteStore implements NoteStore {

  private final Map<String, Map<String, Note>> store = new ConcurrentHashMap<>();

  @Override
  public Note create(String owner, String title, String content) {
    Instant now = Instant.now();
    Note note = new Note(UUID.randomUUID().toString(), owner, title, content, now, now);
    store.computeIfAbsent(owner, k -> new ConcurrentHashMap<>()).put(note.id(), note);
    return note;
  }

  @Override
  public Optional<Note> get(String owner, String noteId) {
    return Optional.ofNullable(store.getOrDefault(owner, Map.of()).get(noteId));
  }

  @Override
  public List<Note> list(String owner) {
    return new ArrayList<>(store.getOrDefault(owner, Map.of()).values());
  }

  @Override
  public boolean delete(String owner, String noteId) {
    Map<String, Note> userNotes = store.get(owner);
    if (userNotes == null) return false;
    return userNotes.remove(noteId) != null;
  }
}
