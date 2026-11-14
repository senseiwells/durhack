import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.internal.os.OperatingSystem
import org.panteleyev.jpackage.ImageType

plugins {
    id("openrndr.common-conventions")
    id("com.gradleup.shadow")
    id("org.panteleyev.jpackageplugin")
    application
}

val applicationMainClass: String = providers.gradleProperty("applicationMainClass").get()

// Packaging properties, override these in gradle.properties
val packageAppName: String = providers.gradleProperty("package.appName").getOrElse("openrndr-application")
val packageAppVersion: String = providers.gradleProperty("package.appVersion").getOrElse("1.0.0")
val packageVendor: String = providers.gradleProperty("package.vendor").getOrElse("OPENRNDR")

val isMac = OperatingSystem.current().isMacOsX
val appImageDirectory = if (isMac) "$packageAppName.app" else packageAppName

application {
    mainClass = providers.gradleProperty("openrndr.application").getOrElse(applicationMainClass)
}

tasks {
    shadowJar {
        isZip64 = true
        manifest {
            attributes["Main-Class"] = applicationMainClass
            attributes["Implementation-Version"] = project.version
        }
    }

    val copyShadowJar = register<Copy>("copyShadowJar") {
        from(shadowJar)
        into(layout.buildDirectory.dir("jars"))
    }

    // Copies data/ next to the directory that -Duser.dir points to
    val copyAssetsToAppImage = register<Copy>("copyAssetsToAppImage") {
        from(layout.projectDirectory) {
            include("data/**")
        }
        into(layout.buildDirectory.dir(if (isMac) "dist/$appImageDirectory/Contents/Resources" else "dist/$appImageDirectory"))
    }

    jpackage {
        dependsOn(copyShadowJar)
        finalizedBy(copyAssetsToAppImage)

        type = ImageType.APP_IMAGE
        input = layout.buildDirectory.dir("jars")
        destination = layout.buildDirectory.dir("dist")
        appName = packageAppName
        appVersion = packageAppVersion
        vendor = packageVendor
        mainJar = shadowJar.get().archiveFileName.get()
        mainClass = applicationMainClass
        addModules = listOf(
            "jdk.unsupported",
            "java.management",
            "java.logging",
            // Required by log4j: xml to parse log4j2.xml, desktop for java.beans, naming to avoid a JNDI warning
            "java.xml",
            "java.desktop",
            "java.naming",
        )

        val javaOptions = listOf(
            "-Dfile.encoding=UTF-8",
            "-XX:+UseCompactObjectHeaders",
            "-XX:+UseZGC",
            "--enable-native-access=ALL-UNNAMED",
        )
        mac {
            this.javaOptions = javaOptions + listOf("-XstartOnFirstThread", "-Duser.dir=\$APPDIR/../Resources")
        }
        windows {
            this.javaOptions = javaOptions + "-Duser.dir=\$APPDIR\\.."
        }
        linux {
            this.javaOptions = javaOptions + "-Duser.dir=\$APPDIR/../.."
        }

        jLinkOptions = listOf(
            "--strip-debug",
            "--compress=2",
            "--no-header-files",
            "--no-man-pages",
        )
    }

    register<Zip>("zipAppImage") {
        group = "distribution"
        dependsOn(jpackage, copyAssetsToAppImage)

        from(layout.buildDirectory.dir("dist")) {
            include("$appImageDirectory/**")
        }
        archiveFileName = "$packageAppName-$packageAppVersion.zip"
        destinationDirectory = layout.buildDirectory.dir("dist")
    }
}

// Shortcuts shown in the OPENRNDR IntelliJ plugin
val openrndrGroup = " 🦌 OPENRNDR"

tasks.register("create executable jar for $applicationMainClass") {
    group = openrndrGroup
    description = "Builds build/libs/${project.name}-${project.version}-all.jar"
    dependsOn(tasks.shadowJar)
}

tasks.register("run $applicationMainClass") {
    group = openrndrGroup
    description = "Launches $applicationMainClass or the class specified via `-Popenrndr.application`"
    dependsOn(tasks.named("run"))
}

tasks.register("create standalone executable for $applicationMainClass") {
    group = openrndrGroup
    description = "Creates build/dist/$packageAppName-$packageAppVersion.zip"
    dependsOn(tasks.named("zipAppImage"))
}

tasks.register("add IDE file scopes") {
    group = openrndrGroup
    description = "Adds Code, Text, Gradle and Media IDE scopes. Enable them to see less files in the project file browser."

    val scopesDirectory = layout.projectDirectory.dir(".idea/scopes").asFile
    doLast {
        scopesDirectory.mkdirs()
        val scopes = listOf(
            "Code" to "file:*.kt||file:*.frag||file:*.vert||file:*.glsl",
            "Text" to "file:*.txt||file:*.md||file:*.xml||file:*.json",
            "Gradle" to "file[*build-logic*]:*/||file:*gradle.*||file:*.gradle||file:*/gradle-wrapper.properties||file:*.toml",
            "Media" to "file:*.png||file:*.jpg||file:*.dds||file:*.exr||file:*.mp3||file:*.wav||file:*.mp4||file:*.mov||file:*.svg",
        )
        for ((name, pattern) in scopes) {
            val file = scopesDirectory.resolve("__$name.xml")
            if (!file.exists()) {
                file.writeText(
                    """
                    <component name="DependencyValidationManager">
                      <scope name=" ★ $name" pattern="$pattern" />
                    </component>
                    """.trimIndent()
                )
            }
        }
    }
}
