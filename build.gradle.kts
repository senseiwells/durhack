plugins {
    id("openrndr.application-conventions")
    // id("openrndr.library-conventions")
}

dependencies {
    implementation(openrndr.bundles.basic)
    implementation(openrndr.bundles.video)
    runtimeOnly(openrndr.bundles.runtime.sdl)
    // runtimeOnly(openrndr.bundles.runtime.glfw)
    runtimeOnly(openrndr.gl3)
    implementation(openrndr.dialogs)
    implementation(openrndr.orextensions)

    implementation(orx.bundles.basic)
    implementation(orx.olive)
    implementation(orx.gui)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.slf4j.api)
    implementation(libs.kotlin.logging)
    runtimeOnly(libs.bundles.logging.full)

    testImplementation(libs.junit)
}
