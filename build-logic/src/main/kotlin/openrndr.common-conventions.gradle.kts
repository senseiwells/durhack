import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.github.ben-manes.versions")
    java
}

val libs = the<LibrariesForLibs>()

val javaVersion: JavaLanguageVersion = JavaLanguageVersion.of(libs.versions.java.get())

group = providers.gradleProperty("project.group").get()
version = providers.gradleProperty("project.version").get()

java {
    toolchain {
        languageVersion.set(javaVersion)
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(javaVersion)
    }
}

// OPENRNDR publishes platform specific natives, so make sure we resolve the ones for this machine.
configurations.matching { it.name.endsWith("runtimeClasspath", ignoreCase = true) }.configureEach {
    attributes {
        attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, objects.named(hostOperatingSystem()))
        attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named(hostArchitecture()))
    }
}

tasks.withType<DependencyUpdatesTask>().configureEach {
    gradleReleaseChannel = "current"
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

if (project == rootProject) {
    tasks.register("openrndrRelease") {
        group = "openrndr template"
        description = "Switch to OPENRNDR and ORX release versions"
        doLast {
            val release = latestOpenrndrVersion(Regex("""refs/tags/v(\d+\.\d+\.\d+)$"""))
            switchOpenrndrVersion(release, allowSonatypeSnapshots = false, allowLocalSnapshots = false)
        }
    }

    tasks.register("openrndrSonatypeSnapshot") {
        group = "openrndr template"
        description = "Switch to OPENRNDR and ORX Sonatype snapshot versions"
        doLast {
            val snapshot = latestOpenrndrVersion(Regex("""refs/tags/v(\d+\.\d+\.\d+)-SNAPSHOT\.\d+"""))
            switchOpenrndrVersion("$snapshot-SNAPSHOT", allowSonatypeSnapshots = true, allowLocalSnapshots = false)
        }
    }

    tasks.register("openrndrLocalSnapshot") {
        group = "openrndr template"
        description = "Switch to OPENRNDR and ORX local snapshot versions"
        doLast {
            val snapshot = latestOpenrndrVersion(Regex("""refs/tags/v(\d+\.\d+\.\d+)-SNAPSHOT\.\d+"""))
            switchOpenrndrVersion("$snapshot-SNAPSHOT", allowSonatypeSnapshots = false, allowLocalSnapshots = true)
        }
    }
}

fun isNonStable(version: String): Boolean {
    return listOf("alpha", "beta", "rc").any { it in version.lowercase() }
}

fun hostOperatingSystem(): String {
    return DefaultNativePlatform.getCurrentOperatingSystem().toFamilyName()
}

fun hostArchitecture(): String {
    return when (val arch = System.getProperty("os.arch")) {
        "x86-64", "x86_64", "amd64" -> "x86-64"
        "arm64", "aarch64" -> "aarch64"
        else -> error("Unsupported architecture $arch")
    }
}

/**
 * Finds the latest OPENRNDR version from the git tags whose first group matches [tag].
 */
fun latestOpenrndrVersion(tag: Regex): String {
    val process = ProcessBuilder("git", "ls-remote", "--tags", "https://github.com/openrndr/openrndr")
        .redirectErrorStream(true)
        .start()
    val output = process.inputStream.bufferedReader().use { it.readText() }
    process.waitFor()

    return output.lines()
        .mapNotNull { tag.find(it)?.groupValues?.get(1) }
        .maxByOrNull { version -> version.split(".").map(String::toInt).fold(0L) { acc, part -> acc * 10_000 + part } }
        ?: error("No OPENRNDR version found matching $tag")
}

fun switchOpenrndrVersion(version: String, allowSonatypeSnapshots: Boolean, allowLocalSnapshots: Boolean) {
    val catalog = rootProject.file("gradle/libs.versions.toml")
    catalog.writeText(
        catalog.readText()
            .replace(Regex("""(?m)^(openrndr\s*=\s*")[^"]*""""), "$1$version\"")
            .replace(Regex("""(?m)^(orx\s*=\s*")[^"]*""""), "$1$version\"")
    )

    val properties = rootProject.file("gradle.properties")
    properties.writeText(
        properties.readText()
            .replace(Regex("""(?m)^openrndr\.allowSonatypeSnapshots\s*=.*$"""), "openrndr.allowSonatypeSnapshots=$allowSonatypeSnapshots")
            .replace(Regex("""(?m)^openrndr\.allowLocalSnapshots\s*=.*$"""), "openrndr.allowLocalSnapshots=$allowLocalSnapshots")
    )

    logger.lifecycle("Switched OPENRNDR and ORX to $version, reload the Gradle project to apply")
}
