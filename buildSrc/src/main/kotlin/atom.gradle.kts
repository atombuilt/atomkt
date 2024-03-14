import org.jetbrains.dokka.gradle.AbstractDokkaLeafTask
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

plugins {
    org.jetbrains.dokka
    org.jetbrains.kotlin.jvm
    `maven-publish`
}

repositories {
    mavenCentral()
    // Repository for PaperMC (https://papermc.io) artifacts.
    maven("https://papermc.io/repo/repository/maven-public/")
    // Repository for SpigotMC (https://spigotmc.org) artifacts.
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    // Repository for maven snapshots.
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}

kotlin {
    explicitApi = ExplicitApiMode.Strict
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        targetCompatibility = "11"
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }

    withType<AbstractDokkaLeafTask> {
        moduleName.set(project.name)
        failOnWarning.set(true)
        dokkaSourceSets.configureEach {
            jdkVersion.set(11)
            suppressGeneratedFiles.set(false)
            sourceLink {
                localDirectory.set(project.projectDir)
                remoteUrl.set(URL("https://github.com/atombuilt/atomkt/blob/${project.gitCommitHash}/${project.name}"))
                remoteLineSuffix.set("#L")
            }
            externalDocumentationLink("https://kotlinlang.org/api/kotlinx.coroutines")
        }
    }
}

publishing {
    publications.register<MavenPublication>(project.name) {
        from(components["java"])
        artifact(tasks.kotlinSourcesJar)
    }
}
