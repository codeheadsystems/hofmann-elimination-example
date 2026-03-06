package com.codeheadsystems.hofmann.example.notes;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryNoteStoreTest {

  private InMemoryNoteStore store;

  @BeforeEach
  void setUp() {
    store = new InMemoryNoteStore();
  }

  @Test
  void createAndList() {
    store.create("alice", "Title", "Content");
    List<Note> notes = store.list("alice");
    assertThat(notes).hasSize(1);
    assertThat(notes.get(0).title()).isEqualTo("Title");
    assertThat(notes.get(0).content()).isEqualTo("Content");
    assertThat(notes.get(0).owner()).isEqualTo("alice");
  }

  @Test
  void listIsUserScoped() {
    store.create("alice", "Alice note", "...");
    store.create("bob", "Bob note", "...");
    assertThat(store.list("alice")).hasSize(1);
    assertThat(store.list("bob")).hasSize(1);
    assertThat(store.list("alice").get(0).title()).isEqualTo("Alice note");
  }

  @Test
  void listEmptyForUnknownUser() {
    assertThat(store.list("nobody")).isEmpty();
  }

  @Test
  void get() {
    Note created = store.create("alice", "T", "C");
    Optional<Note> found = store.get("alice", created.id());
    assertThat(found).isPresent();
    assertThat(found.get().id()).isEqualTo(created.id());
  }

  @Test
  void getWrongOwnerReturnsEmpty() {
    Note created = store.create("alice", "T", "C");
    assertThat(store.get("bob", created.id())).isEmpty();
  }

  @Test
  void delete() {
    Note created = store.create("alice", "T", "C");
    assertThat(store.delete("alice", created.id())).isTrue();
    assertThat(store.list("alice")).isEmpty();
  }

  @Test
  void deleteWrongOwnerReturnsFalse() {
    Note created = store.create("alice", "T", "C");
    assertThat(store.delete("bob", created.id())).isFalse();
    assertThat(store.list("alice")).hasSize(1);
  }

  @Test
  void deleteNonExistentReturnsFalse() {
    assertThat(store.delete("alice", "no-such-id")).isFalse();
  }
}
