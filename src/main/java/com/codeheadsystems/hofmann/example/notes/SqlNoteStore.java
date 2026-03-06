package com.codeheadsystems.hofmann.example.notes;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * H2-backed NoteStore. Creates its schema on construction.
 */
public class SqlNoteStore implements NoteStore {

  private static final Logger log = LoggerFactory.getLogger(SqlNoteStore.class);

  private final Jdbi jdbi;

  public SqlNoteStore(Jdbi jdbi) {
    this.jdbi = jdbi;
    initSchema();
  }

  private void initSchema() {
    log.info("Initialising notes schema");
    jdbi.useHandle(h -> {
      h.execute("""
          CREATE TABLE IF NOT EXISTS notes (
              id         VARCHAR(36)    PRIMARY KEY,
              owner      VARCHAR(4096)  NOT NULL,
              title      VARCHAR(1000)  NOT NULL,
              content    VARCHAR(32768) NOT NULL,
              created_at TIMESTAMP      NOT NULL,
              updated_at TIMESTAMP      NOT NULL
          )
          """);
      h.execute("CREATE INDEX IF NOT EXISTS idx_notes_owner ON notes(owner)");
    });
  }

  @Override
  public Note create(String owner, String title, String content) {
    Instant now = Instant.now();
    String id = UUID.randomUUID().toString();
    jdbi.useHandle(h -> h.createUpdate("""
        INSERT INTO notes (id, owner, title, content, created_at, updated_at)
        VALUES (:id, :owner, :title, :content, :createdAt, :updatedAt)
        """)
        .bind("id", id)
        .bind("owner", owner)
        .bind("title", title)
        .bind("content", content)
        .bind("createdAt", Timestamp.from(now))
        .bind("updatedAt", Timestamp.from(now))
        .execute());
    return new Note(id, owner, title, content, now, now);
  }

  @Override
  public Optional<Note> get(String owner, String noteId) {
    return jdbi.withHandle(h -> h.createQuery("""
        SELECT id, owner, title, content, created_at, updated_at
        FROM notes WHERE id = :id AND owner = :owner
        """)
        .bind("id", noteId)
        .bind("owner", owner)
        .map((rs, ctx) -> new Note(
            rs.getString("id"),
            rs.getString("owner"),
            rs.getString("title"),
            rs.getString("content"),
            rs.getTimestamp("created_at").toInstant(),
            rs.getTimestamp("updated_at").toInstant()))
        .findFirst());
  }

  @Override
  public List<Note> list(String owner) {
    return jdbi.withHandle(h -> h.createQuery("""
        SELECT id, owner, title, content, created_at, updated_at
        FROM notes WHERE owner = :owner ORDER BY created_at DESC
        """)
        .bind("owner", owner)
        .map((rs, ctx) -> new Note(
            rs.getString("id"),
            rs.getString("owner"),
            rs.getString("title"),
            rs.getString("content"),
            rs.getTimestamp("created_at").toInstant(),
            rs.getTimestamp("updated_at").toInstant()))
        .list());
  }

  @Override
  public boolean delete(String owner, String noteId) {
    return jdbi.withHandle(h -> h.createUpdate("""
        DELETE FROM notes WHERE id = :id AND owner = :owner
        """)
        .bind("id", noteId)
        .bind("owner", owner)
        .execute()) > 0;
  }
}
