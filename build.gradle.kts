plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.thteam"
version = "1.0.0"
description = "THCore - Core library plugin for Paper 1.21.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")                          // Paper
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")        // PlaceholderAPI
    maven("https://maven.enginehub.org/repo/")                                          // WorldGuard / WorldEdit
    maven("https://repo.nexomc.com/releases")                                           // Nexo
    maven("https://mvn.lumine.io/repository/maven-public/")                             // MythicMobs
    maven("https://repo.nightexpressdev.com/releases")                                  // ExcellentEconomy / CoinsEngine
    maven("https://repo.rosewooddev.io/repository/public/")                             // PlayerPoints
    maven("https://jitpack.io")                                                         // ItemsAdder
}

dependencies {
    // Paper API - provided by server at runtime
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")

    // HikariCP + JDBC drivers - shaded into the jar
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
    implementation("com.mysql:mysql-connector-j:8.4.0")

    // ==================== Soft-depend APIs (compileOnly - NEVER shade) ====================
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.12")
    compileOnly("com.nexomc:nexo:0.7.0")
    compileOnly("io.lumine:Mythic-Dist:5.6.1")
    compileOnly("su.nightexpress.coinsengine:CoinsEngine:1.9.0")
    compileOnly("org.black_ixx:playerpoints:3.2.7")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.3-beta-14b")
    // compileOnly("io.th0rgal:oraxen:<version>")  // descomentar cuando confirmes la versión
}

tasks {
    shadowJar {
        archiveClassifier.set("")

        // Relocalizar libs shadeadas para evitar conflictos con otros plugins
        relocate("com.zaxxer.hikari", "com.thteam.thcore.libs.hikari")
        relocate("org.sqlite", "com.thteam.thcore.libs.sqlite")
        relocate("com.mysql", "com.thteam.thcore.libs.mysql")

        minimize()
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
}
