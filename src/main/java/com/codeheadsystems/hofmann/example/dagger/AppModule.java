package com.codeheadsystems.hofmann.example.dagger;

import com.codeheadsystems.hofmann.example.notes.NoteStore;
import com.codeheadsystems.hofmann.example.notes.NotesResource;
import com.codeheadsystems.hofmann.example.notes.SqlNoteStore;
import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import org.jdbi.v3.core.Jdbi;

@Module
public class AppModule {

  private final Jdbi jdbi;

  public AppModule(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Provides
  @Singleton
  NoteStore provideNoteStore() {
    return new SqlNoteStore(jdbi);
  }

  @Provides
  @Singleton
  NotesResource provideNotesResource(NoteStore noteStore) {
    return new NotesResource(noteStore);
  }
}
