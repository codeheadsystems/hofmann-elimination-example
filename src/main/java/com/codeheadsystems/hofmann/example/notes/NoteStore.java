package com.codeheadsystems.hofmann.example.notes;

import java.util.List;
import java.util.Optional;

public interface NoteStore {

  Note create(String owner, String title, String content);

  Optional<Note> get(String owner, String noteId);

  List<Note> list(String owner);

  boolean delete(String owner, String noteId);
}
