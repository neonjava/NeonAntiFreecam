plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

group = "me.neonjava"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.11.2")
    // Let's use packetevents-spigot 2.11.2 as we checked its metadata or 2.7.0. Let's use 2.7.0 if that's standard, or 2.11.2.
    // Let's use 2.7.0 as it matches standard releases. Actually, let's use 2.11.2 since CFR decompile in previous plugin showed com.github.retrooper.packetevents.
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveClassifier.set("")
        // Relocate libraries if needed
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}
