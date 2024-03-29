plugins {
    `maven-publish`
    `java-library`
    signing
}

publishing {
    publications {
        withType<MavenPublication>().configureEach {
            artifact(
                tasks.register<Jar>("${name}DokkaJar") {
                    archiveClassifier.set("javadoc")
                    destinationDirectory.set(destinationDirectory.get().dir(name))
                    from(tasks.named("dokkaHtml"))
                }
            )

            groupId = "com.atombuilt.atomkt"
            artifactId = project.name
            version = gitModuleVersion

            pom {
                name.set(project.path.removePrefix(":").replace(":", "-"))
                description.set("AtomBuilt libraries")
                url.set("https://github.com/atombuilt/atomkt")

                developers {
                    developer {
                        name.set("AtomBuilt")
                        url.set("https://atombuilt.com")
                        email.set("contact@atombuilt.com")
                        timezone.set("Europe/Warsaw")
                    }
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/atombuilt/atomkt/issues")
                }
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/license/mit/")
                    }
                }
                scm {
                    connection.set("scm:git:ssh://github.com/atombuilt/atomkt.git")
                    developerConnection.set("scm:git:ssh://git@github.com:atombuilt/atomkt.git")
                    url.set("https://github.com/atombuilt/atomkt")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri(sonatypeRepository(isRelease))
            credentials {
                username = SONATYPE_USER
                password = SONATYPE_PASSWORD
            }
        }
    }
}

if (isRelease) {
    signing {
        val secretKey = findProperty("signingKey")?.toString()
        val password = findProperty("signingPassword")?.toString()
        if (secretKey != null && password != null) {
            useInMemoryPgpKeys(secretKey, password)
        }
        sign(publishing.publications)
    }
}
