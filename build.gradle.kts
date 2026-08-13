import java.nio.charset.Charset

plugins {
    id("java")
    id("maven-publish")
    id("io.freefair.lombok") version "9.5.0"
    id("xyz.jpenilla.run-paper") version "3.0.1"
    id("de.eldoria.plugin-yml.bukkit") version "0.9.0"
}

group = "dev.lumas.shops"
version = try {
    ProcessBuilder("git", "rev-parse", "--short", "HEAD")
        .redirectErrorStream(true)
        .start()
        .inputStream
        .bufferedReader(Charset.defaultCharset())
        .readText()
        .trim()
        .ifBlank { "none" }
} catch (e: Exception) {
    e.printStackTrace()
    "none"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.jsinco.dev/releases")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    compileOnly("org.spongepowered:configurate-yaml:4.2.0")
    compileOnly("dev.lumas.core:LumaCore:dd53fbc")
    compileOnly("dev.lumas.lumaitems:LumaItems:42e7303")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        isTransitive = false
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "jsinco-repo"
            url = uri("https://repo.jsinco.dev/releases")
            credentials {
                // get from environment
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
        }
    }
}

tasks.runServer {
    minecraftVersion("26.1.2")
}

bukkit {
    name = "Shops"
    main = "dev.lumas.shops.Shops"
    version = project.version.toString()
    apiVersion = "1.21"
    foliaSupported = true
    depend = listOf("LumaCore")
    softDepend = listOf("LumaItems")
}