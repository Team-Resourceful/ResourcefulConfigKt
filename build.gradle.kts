import com.teamresourceful.publishing.GitHubPom
import com.teamresourceful.publishing.javaPublishing
import com.teamresourceful.utils.Platform
import com.teamresourceful.utils.getPlatform

plugins {
    java
    kotlin("jvm") version "2.3.0"
    id("maven-publish")
    alias(libs.plugins.resourceful.gradle)
    alias(libs.plugins.resourceful.minecraft) apply false
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    val platform = getPlatform()

    when (platform) {
        Platform.COMMON -> apply(plugin = "com.teamresourceful.plugins.minecraft-platform-common")
        Platform.FABRIC -> apply(plugin = "com.teamresourceful.plugins.minecraft-platform-fabric")
        Platform.NEOFORGE -> apply(plugin = "com.teamresourceful.plugins.minecraft-platform-neoforge")
    }

    repositories {
        maven("https://prmaven.neoforged.net/NeoForge/pr2879")
        mavenCentral()
    }

    dependencies {
        compileOnly("org.jetbrains.kotlin:kotlin-reflect:2.2.0")

        if (platform == Platform.FABRIC) {
            compileOnly(rootProject.libs.modmenu)
        }

        val configVersion = rootProject.libs.versions.resourceful.config.get()
        compileOnly("com.teamresourceful.resourcefulconfig:resourcefulconfig-${platform.id}-${rootProject.libs.versions.minecraft.get()}:$configVersion")
    }

    if (platform == Platform.COMMON) {
        javaPublishing {
            artifactId = "${rootProject.name}-${rootProject.libs.versions.minecraft.get()}".lowercase()

            pom = GitHubPom(
                "ResourcefulConfigKt",
                "Crossplatform config library for Team Resourceful mods and more.",
                "MIT",
                "https://github.com/Team-Resourceful/ResourcefulConfigKt"
            )

            repo = "https://maven.teamresourceful.com/repository/maven-releases/"
        }
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
