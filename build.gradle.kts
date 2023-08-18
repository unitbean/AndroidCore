plugins {
    id("com.android.application") version "8.1.0" apply false
    id("com.android.library") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("org.jetbrains.dokka") version "1.8.20" apply false
    id("com.vanniktech.maven.publish") version "0.25.3" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.5.3" apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.12" apply false
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
}

task<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}