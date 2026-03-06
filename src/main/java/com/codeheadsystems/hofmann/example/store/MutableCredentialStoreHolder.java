package com.codeheadsystems.hofmann.example.store;

import com.codeheadsystems.hofmann.server.store.CredentialStore;
import com.codeheadsystems.rfc.opaque.model.RegistrationRecord;
import java.util.Optional;

/**
 * A CredentialStore that delegates to a real implementation set after construction.
 *
 * This exists because HofmannBundle is added during initialize() before the database
 * is available, but the real store is only ready in run(). The delegate is always set
 * before the server starts accepting connections, so there is no race.
 */
public class MutableCredentialStoreHolder implements CredentialStore {

  private volatile CredentialStore delegate;

  public void setDelegate(CredentialStore delegate) {
    this.delegate = delegate;
  }

  @Override
  public void store(byte[] credentialIdentifier, RegistrationRecord record) {
    delegate.store(credentialIdentifier, record);
  }

  @Override
  public Optional<RegistrationRecord> load(byte[] credentialIdentifier) {
    return delegate.load(credentialIdentifier);
  }

  @Override
  public void delete(byte[] credentialIdentifier) {
    delegate.delete(credentialIdentifier);
  }
}
