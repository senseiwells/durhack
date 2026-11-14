plugins {
    id("openrndr.common-conventions")
    `maven-publish`
}

// The openrndr catalog is created in the root settings from a Maven artifact,
// so it isn't available as a type-safe accessor inside build-logic.
val openrndr: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("openrndr")

val main: SourceSet = sourceSets.getByName("main")
val demo: SourceSet = sourceSets.create("demo") {
    compileClasspath += main.compileClasspath + main.output
    runtimeClasspath += main.runtimeClasspath + main.output
}

dependencies {
    "demoRuntimeOnly"(openrndr.findLibrary("gl3").get())
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
