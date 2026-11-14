plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugins.kotlin.jvm.asDependency())
    implementation(libs.plugins.kotlin.serialization.asDependency())
    implementation(libs.plugins.shadow.asDependency())
    implementation(libs.plugins.jpackage.asDependency())
    implementation(libs.plugins.versions.asDependency())

    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

fun Provider<PluginDependency>.asDependency() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version.requiredVersion}"
}
