import org.gradle.api.Project
import java.io.ByteArrayOutputStream

private val Project.gitTag: String?
    get() = git("tag", "--no-column", "--points-at", "HEAD")
        .takeIf { it.isNotBlank() }
        ?.lines()
        ?.single()

val Project.gitModuleVersion: String get() = gitTag ?: "${git("branch", "--show-current").replace("/", "-")}-SNAPSHOT"

val Project.gitCommitHash get() = git("rev-parse", "--verify", "HEAD")
val Project.isRelease: Boolean get() = gitTag != null

private fun Project.git(vararg command: String): String {
    val output = ByteArrayOutputStream()
    exec {
        commandLine("git", *command)
        standardOutput = output
        errorOutput = output
        workingDir = rootDir
    }.rethrowFailure().assertNormalExitValue()
    return output.toString().trim()
}
