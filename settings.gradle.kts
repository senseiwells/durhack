pluginManagement {
    includeBuild("build-logic")

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val allowSonatypeSnapshots = providers.gradleProperty("openrndr.allowSonatypeSnapshots").orNull.toBoolean()
val allowLocalSnapshots = providers.gradleProperty("openrndr.allowLocalSnapshots").orNull.toBoolean()

dependencyResolutionManagement {
    // Repositories live here (rather than in the conventions) because
    // the openrndr and orx catalogs below are resolved from them.
    repositories {
        mavenCentral()

        if (allowLocalSnapshots) {
            mavenLocal {
                content {
                    includeGroup("org.openrndr")
                    includeGroup("org.openrndr.extra")
                }
            }
        }

        if (allowSonatypeSnapshots) {
            maven("https://central.sonatype.com/repository/maven-snapshots/") {
                name = "Central Portal Snapshots"
                content {
                    includeGroup("org.openrndr")
                    includeGroup("org.openrndr.extra")
                }
            }
        }
    }

    versionCatalogs {
        // libs is imported automatically from gradle/libs.versions.toml
        create("openrndr") {
            from("org.openrndr:openrndr-module-catalog:${catalogVersion("openrndr")}")
        }
        create("orx") {
            from("org.openrndr.extra:orx-module-catalog:${catalogVersion("orx")}")
        }
    }
}

rootProject.name = providers.gradleProperty("project.name").get()

// There is no public Gradle API to read a version from a catalog in settings, so we parse it ourselves.
fun catalogVersion(key: String): String {
    val catalog = file("gradle/libs.versions.toml").readText()
    val regex = Regex("""^${Regex.escape(key)}\s*=\s*(?:\{\s*require\s*=\s*)?"([^"]*)"""", RegexOption.MULTILINE)
    return regex.find(catalog)?.groupValues?.get(1) ?: error("Can't find '$key' version in gradle/libs.versions.toml")
}
