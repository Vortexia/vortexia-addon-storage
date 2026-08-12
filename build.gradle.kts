plugins {
    java
    id("com.gradleup.shadow") version "9.4.1"
    id("io.papermc.hangar-publish-plugin") version "0.1.4"
    id("com.modrinth.minotaur") version "2.9.0"
}

group = "me.alikuxac.vortexia"
val refName = System.getenv("GITHUB_REF_NAME") ?: "local"
val refType = System.getenv("GITHUB_REF_TYPE") ?: "branch"
val runNumber = System.getenv("GITHUB_RUN_NUMBER") ?: "0"
val versionNumber = project.findProperty("projectVersion") as? String ?: "0.1.0"

val isMasterRelease = refType == "tag"
val isBetaRelease = isMasterRelease && refName.contains("beta", ignoreCase = true)

val calculatedVersion = if (refType == "tag") {
    refName.replaceFirst("v", "")
} else if (refName == "master" || refName == "main") {
    versionNumber
} else if (refName == "development") {
    if (runNumber != "0") "$versionNumber-alpha-b.$runNumber" else "$versionNumber-alpha"
} else {
    if (runNumber != "0") "$versionNumber-${refName.uppercase()}-$runNumber" else "$versionNumber-${refName.uppercase()}"
}

version = calculatedVersion

val hangarChannel = when {
    isBetaRelease -> "Beta"
    isMasterRelease -> "Release"
    else -> "Snapshot"
}

val modrinthVersionType = when {
    isBetaRelease -> "beta"
    isMasterRelease -> "release"
    else -> "alpha"
}

val finalVersionName = when {
    isBetaRelease -> refName
    isMasterRelease -> refName
    else -> "v$versionNumber-alpha (build #$runNumber)"
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
        compileOnly("me.alikuxac.vortexia:vortexia-api:1.3.1")
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

val mcVersionsProp = project.findProperty("mcVersions") as? String ?: "1.21"
val parsedMcVersions = mcVersionsProp.split(",").map { it.trim() }
val modrinthProjectIdProp = project.findProperty("modrinthProjectID") as? String ?: "vortexia-addon-storage"
val hangarProjectIdProp = project.findProperty("hangarProjectID") as? String ?: "vortexia-addon-storage"
val gitChangelog = System.getenv("COMMIT_MESSAGE") ?: "No changelog provided."

hangarPublish {
    publications.register("plugin") {
        version.set(calculatedVersion)
        id.set(hangarProjectIdProp)
        channel.set(hangarChannel)
        changelog.set(gitChangelog)
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))

        platforms {
            register(io.papermc.hangarpublishplugin.model.Platforms.PAPER) {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set(parsedMcVersions)
            }
        }
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set(modrinthProjectIdProp)
    versionNumber.set(calculatedVersion)
    versionName.set(finalVersionName)
    versionType.set(modrinthVersionType)
    changelog.set(gitChangelog)
    uploadFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    gameVersions.set(parsedMcVersions)
    loaders.set(listOf("paper"))
}
