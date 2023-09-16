import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    atom
    id("xyz.jpenilla.run-paper") version "2.1.0"
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":spigot"))
    compileOnly(libs.spigotmc)
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled
}


tasks {
    runServer {
        minecraftVersion("1.20.1")
    }
}
