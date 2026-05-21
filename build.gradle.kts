plugins {
    id("java")
    id("io.freefair.lombok") version "9.5.0"
    id("xyz.jpenilla.run-paper") version "3.0.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "dev.lumas.shops"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.jsinco.dev/releases")
    maven("https://jitpack.io")
}

dependencies {
    //compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    paperweight.paperDevBundle("26.1.2.build.+")
    compileOnly("dev.lumas.core:LumaCore:0383263")
    compileOnly("dev.lumas.lumaitems:LumaItems:42e7303")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        isTransitive = false
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.runServer {
    minecraftVersion("26.1.2")
}