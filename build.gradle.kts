buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.1")
        classpath(kotlin("gradle-plugin", version = "1.5.30"))
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.32")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.17.0")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}