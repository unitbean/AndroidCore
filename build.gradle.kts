plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.dokka) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.android.navigation.safeargs) apply false
    alias(libs.plugins.devtools.ksp) apply false

}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
}

task<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}