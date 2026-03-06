package com.codeheadsystems.hofmann.example.dagger;

import com.codeheadsystems.hofmann.example.notes.NotesResource;
import dagger.Component;
import jakarta.inject.Singleton;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

  NotesResource notesResource();
}
