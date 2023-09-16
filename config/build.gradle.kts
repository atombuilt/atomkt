plugins {
    atom
    published
}

dependencies {
    api(project(":commons"))
    api(kotlin("reflect"))
    api(libs.hoplite.yaml)
    api(libs.koin.core)
}
