plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

group = "com.atombuilt.atomkt"
version = "1.0.0"

subprojects {
    group = "${rootProject.group}.atomkt"
    version = rootProject.version
}
