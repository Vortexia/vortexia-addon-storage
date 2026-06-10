plugins {
    java
    id("com.gradleup.shadow") version "9.4.1"
}

group = "me.alikuxac.vortexia"
val refName = System.getenv("GITHUB_REF_NAME") ?: "local"
val refType = System.getenv("GITHUB_REF_TYPE") ?: "branch"

version = if (refType == "tag") {
    refName.replaceFirst("v", "")
} else if (refName == "master" || refName == "main") {
    "0.1.0"
} else if (refName == "development") {
    "0.1.0-DEV"
} else {
    "0.1.0-${refName.uppercase()}"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    if (findProject(":vortexia-api") != null) {
        compileOnly(project(":vortexia-api"))
    } else {
        compileOnly("me.alikuxac.vortexia:vortexia-api:1.2.2")
    }
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    processResources {
        val projectVersion = project.version
        inputs.property("version", projectVersion)
        filesMatching("**/paper-plugin.yml") {
            expand("version" to projectVersion)
        }
    }

    shadowJar {
        archiveClassifier.set("")
        mergeServiceFiles()
    }

    jar {
        enabled = false
    }

    build {
        dependsOn(shadowJar)
    }
}
