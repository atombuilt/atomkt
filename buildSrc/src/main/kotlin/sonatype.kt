import java.lang.System.getenv

const val SONATYPE_RELEASE_REPOSITORY = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
const val SONATYPE_SNAPSHOT_REPOSITORY = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
val SONATYPE_USER: String? get() = getenv("NEXUS_USER")
val SONATYPE_PASSWORD: String? get() = getenv("NEXUS_PASSWORD")

fun sonatypeRepository(isRelease: Boolean): String {
    return if (isRelease) SONATYPE_RELEASE_REPOSITORY else SONATYPE_SNAPSHOT_REPOSITORY
}
