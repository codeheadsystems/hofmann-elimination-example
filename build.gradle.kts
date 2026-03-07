import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    id("com.gradleup.shadow") version "9.3.2"
}

group = "com.codeheadsystems.hofmann"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass.set("com.codeheadsystems.hofmann.example.ExampleApplication")
}

dependencies {
    implementation("com.codeheadsystems:hofmann-dropwizard:1.2.0")
    implementation("io.dropwizard:dropwizard-core:5.0.1")
    implementation("io.dropwizard:dropwizard-auth:5.0.1")
    implementation("io.dropwizard:dropwizard-assets:5.0.1")
    implementation("com.google.dagger:dagger:2.59.2")
    annotationProcessor("com.google.dagger:dagger-compiler:2.59.2")

    // Database
    implementation("com.h2database:h2:2.4.240")
    implementation("org.jdbi:jdbi3-core:3.51.0")
    implementation("com.zaxxer:HikariCP:7.0.2")

    testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:unchecked")
}

// ── Frontend build ────────────────────────────────────────────────────────────

val npmInstall = tasks.register<Exec>("npmInstall") {
    workingDir = file("frontend")
    commandLine("npm", "install")
}

val npmBuild = tasks.register<Exec>("npmBuild") {
    dependsOn(npmInstall)
    workingDir = file("frontend")
    commandLine("npm", "run", "build")
}

val generatedFrontendDir = layout.buildDirectory.dir("generated-frontend")

val copyFrontend = tasks.register<Copy>("copyFrontend") {
    dependsOn(npmBuild)
    from(file("frontend/dist"))
    into(generatedFrontendDir.map { it.dir("frontend") })
}

// Expose generated directory as a resource source so processResources picks it up
sourceSets.main {
    resources.srcDir(generatedFrontendDir)
}

tasks.named("processResources") {
    dependsOn(copyFrontend)
}

// Remove H2 database files on clean (keeps the .emptydir placeholder)
tasks.named<Delete>("clean") {
    delete(fileTree("data") { include("*.db") })
}

// Merge META-INF/services/* so Dropwizard's connector/provider registrations survive fat-jar packaging
tasks.withType<ShadowJar>().configureEach {
    mergeServiceFiles()
}
