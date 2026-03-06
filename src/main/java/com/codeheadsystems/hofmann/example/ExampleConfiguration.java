package com.codeheadsystems.hofmann.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.codeheadsystems.hofmann.dropwizard.HofmannConfiguration;
import jakarta.validation.constraints.NotEmpty;

public class ExampleConfiguration extends HofmannConfiguration {

  /** JDBC URL for H2 file-mode database. Path is relative to the working directory. */
  @NotEmpty
  @JsonProperty
  private String databaseUrl = "jdbc:h2:file:./data/hofmann-example";

  @JsonProperty
  private String databaseUser = "sa";

  @JsonProperty
  private String databasePassword = "";

  public String getDatabaseUrl() { return databaseUrl; }
  public void setDatabaseUrl(String databaseUrl) { this.databaseUrl = databaseUrl; }

  public String getDatabaseUser() { return databaseUser; }
  public void setDatabaseUser(String databaseUser) { this.databaseUser = databaseUser; }

  public String getDatabasePassword() { return databasePassword; }
  public void setDatabasePassword(String databasePassword) { this.databasePassword = databasePassword; }
}
