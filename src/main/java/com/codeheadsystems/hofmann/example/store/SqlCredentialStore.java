package com.codeheadsystems.hofmann.example.store;

import com.codeheadsystems.hofmann.server.store.CredentialStore;
import com.codeheadsystems.rfc.opaque.model.Envelope;
import com.codeheadsystems.rfc.opaque.model.RegistrationRecord;
import java.util.Optional;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * H2-backed CredentialStore. Creates its schema on construction.
 *
 * Uses H2's MERGE INTO ... KEY (...) for idempotent upsert so that
 * re-registration replaces the existing record cleanly.
 */
public class SqlCredentialStore implements CredentialStore {

  private static final Logger log = LoggerFactory.getLogger(SqlCredentialStore.class);

  private final Jdbi jdbi;

  public SqlCredentialStore(Jdbi jdbi) {
    this.jdbi = jdbi;
    initSchema();
  }

  private void initSchema() {
    log.info("Initialising credentials schema");
    jdbi.useHandle(h -> h.execute("""
        CREATE TABLE IF NOT EXISTS credentials (
            credential_id    VARBINARY(4096) PRIMARY KEY,
            client_public_key VARBINARY(256)  NOT NULL,
            masking_key       VARBINARY(256)  NOT NULL,
            envelope_nonce    VARBINARY(64)   NOT NULL,
            auth_tag          VARBINARY(256)  NOT NULL
        )
        """));
  }

  @Override
  public void store(byte[] credentialIdentifier, RegistrationRecord record) {
    jdbi.useHandle(h -> h.createUpdate("""
        MERGE INTO credentials
            (credential_id, client_public_key, masking_key, envelope_nonce, auth_tag)
            KEY (credential_id)
            VALUES (:cid, :cpk, :mk, :en, :at)
        """)
        .bind("cid", credentialIdentifier)
        .bind("cpk", record.clientPublicKey())
        .bind("mk",  record.maskingKey())
        .bind("en",  record.envelope().envelopeNonce())
        .bind("at",  record.envelope().authTag())
        .execute());
  }

  @Override
  public Optional<RegistrationRecord> load(byte[] credentialIdentifier) {
    return jdbi.withHandle(h -> h.createQuery("""
        SELECT client_public_key, masking_key, envelope_nonce, auth_tag
        FROM credentials WHERE credential_id = :cid
        """)
        .bind("cid", credentialIdentifier)
        .map((rs, ctx) -> new RegistrationRecord(
            rs.getBytes("client_public_key"),
            rs.getBytes("masking_key"),
            new Envelope(rs.getBytes("envelope_nonce"), rs.getBytes("auth_tag"))))
        .findFirst());
  }

  @Override
  public void delete(byte[] credentialIdentifier) {
    jdbi.useHandle(h -> h.createUpdate(
        "DELETE FROM credentials WHERE credential_id = :cid")
        .bind("cid", credentialIdentifier)
        .execute());
  }
}
