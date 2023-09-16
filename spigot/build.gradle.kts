plugins {
    atom
    published
}

dependencies {
    api(project(":commons"))
    api(libs.coroutines)
    api(libs.logging)
    api(kotlin("reflect"))
    api(libs.koin.core)
    api(project(":config"))
    compileOnly(libs.spigotmc)
}

kotlin {
    sourceSets.forEach {
        it.languageSettings {
            optIn("kotlinx.coroutines.InternalCoroutinesApi")
            optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            optIn("kotlin.ExperimentalStdlibApi")
        }
    }
}
