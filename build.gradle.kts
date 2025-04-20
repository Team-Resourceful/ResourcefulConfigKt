import com.teamresourceful.publishing.GitHubPom
import com.teamresourceful.publishing.javaPublishing
import com.teamresourceful.utils.Platform
import com.teamresourceful.utils.getPlatform

plugins {
    java
    kotlin("jvm") version "2.0.20"
    id("maven-publish")
    alias(libs.plugins.resourceful.loom)
    alias(libs.plugins.resourceful.gradle)
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    val platform = getPlatform()
    val mcVersion = rootProject.libs.versions.minecraft.get()

    dependencies {
        if (platform == Platform.FABRIC) {
            "modCompileOnly"(rootProject.libs.modmenu)
        }

        if (platform == Platform.COMMON) {
            implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.20")
        }

        val configVersion = rootProject.libs.versions.resourceful.config.get()
        "modCompileOnly"("com.teamresourceful.resourcefulconfig:resourcefulconfig-${platform.id}-$mcVersion:$configVersion")
    }

    javaPublishing {
        artifactId = "${rootProject.name}-${platform.name}-$mcVersion".lowercase()

        pom = GitHubPom(
            "ResourcefulConfigKt",
            "Crossplatform config library for Team Resourceful mods and more.",
            "MIT",
            "https://github.com/Team-Resourceful/ResourcefulConfigKt"
        )

        repo = "https://maven.teamresourceful.com/repository/maven-releases/"
    }
}


resourcefulGradle {
    templates {
        register("readme") {
            source = file("templates/README.md.template")
            injectedValues = mapOf(
                "version" to version,
                "minecraft" to libs.versions.minecraft.get(),
            )
        }
    }
}
