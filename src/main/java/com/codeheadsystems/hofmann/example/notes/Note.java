package com.codeheadsystems.hofmann.example.notes;

import java.time.Instant;

public record Note(
    String id,
    String owner,
    String title,
    String content,
    Instant createdAt,
    Instant updatedAt
) {}
