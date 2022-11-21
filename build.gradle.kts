plugins {
    id("com.android.application") version "7.3.1" apply false
    id("org.jetbrains.kotlin.android") version "1.7.21" apply false
    id("org.jetbrains.dokka") version "1.6.10" apply false
    id("com.vanniktech.maven.publish") version "0.21.0" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.5.3" apply false
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}