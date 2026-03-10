package com.codeheadsystems.hofmann.example;

import com.codeheadsystems.hofmann.dropwizard.HofmannBundle;
import com.codeheadsystems.hofmann.example.dagger.AppComponent;
import com.codeheadsystems.hofmann.example.dagger.AppModule;
import com.codeheadsystems.hofmann.example.dagger.DaggerAppComponent;
import com.codeheadsystems.hofmann.example.recovery.DemoRecoveryChallenger;
import com.codeheadsystems.hofmann.example.store.MutableCredentialStoreHolder;
import com.codeheadsystems.hofmann.example.store.SqlCredentialStore;
import com.codeheadsystems.hofmann.server.store.InMemorySessionStore;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.lifecycle.Managed;
import org.jdbi.v3.core.Jdbi;

public class ExampleApplication extends Application<ExampleConfiguration> {

  // Created in initialize(), populated in run() before the server starts.
  private final MutableCredentialStoreHolder credentialStoreHolder = new MutableCredentialStoreHolder();

  public static void main(String[] args) throws Exception {
    new ExampleApplication().run(args);
  }

  @Override
  public String getName() {
    return "hofmann-elimination-example";
  }

  @Override
  public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
    // Serve the frontend SPA at "/". Jersey is scoped to "/api/*" via server.rootPath
    // in example.yml, so there is no conflict between jersey resources and static assets.
    bootstrap.addBundle(new AssetsBundle("/frontend/", "/", "index.html", "frontend"));
    // Use the credential holder so the bundle wires up before the database is available,
    // but all actual credential access happens during request processing (after run()).
    bootstrap.addBundle(new HofmannBundle<>(credentialStoreHolder, new InMemorySessionStore(), null)
        .withRecovery(new DemoRecoveryChallenger()));
  }

  @Override
  public void run(ExampleConfiguration config, Environment env) {
    HikariConfig hc = new HikariConfig();
    hc.setJdbcUrl(config.getDatabaseUrl());
    hc.setUsername(config.getDatabaseUser());
    hc.setPassword(config.getDatabasePassword());
    hc.setMaximumPoolSize(10);
    hc.setMinimumIdle(2);
    HikariDataSource dataSource = new HikariDataSource(hc);
    env.lifecycle().manage(new Managed() {
      public void start() {}
      public void stop() { dataSource.close(); }
    });

    Jdbi jdbi = Jdbi.create(dataSource);

    // Wire the real SQL-backed credential store before the server accepts connections.
    credentialStoreHolder.setDelegate(new SqlCredentialStore(jdbi));

    AppComponent component = DaggerAppComponent.builder()
        .appModule(new AppModule(jdbi))
        .build();
    env.jersey().register(component.notesResource());
  }
}
