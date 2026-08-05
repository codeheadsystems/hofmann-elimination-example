import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    alias(libs.plugins.shadow)
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
    implementation(libs.hofmann.dropwizard)
    implementation(libs.dropwizard.core)
    implementation(libs.dropwizard.auth)
    implementation(libs.dropwizard.assets)
    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)

    // Database
    implementation(libs.h2)
    implementation(libs.jdbi3.core)
    implementation(libs.hikaricp)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testRuntimeOnly(libs.junit.platform.launcher)
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
