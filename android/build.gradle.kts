import org.jetbrains.kotlin.config.KotlinCompilerVersion

buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.14.2")
    }
}

apply(plugin = "com.vanniktech.maven.publish")

plugins {
    id("com.android.library")
    id("org.jetbrains.dokka")
    kotlin("android")
}

repositories {
    mavenCentral()
}

tasks.dokkaJavadoc.configure {
    outputDirectory.set(buildDir.resolve("javadoc"))
}

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(16)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.8.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    buildFeatures {
        buildConfig = false
    }
}

val verMoxy = "2.2.2"
val verDagger = "2.37"
val verCoroutines = "1.5.0"
val verRetrofit = "2.9.0"

dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    testImplementation("org.mockito:mockito-core:3.11.0")
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$verCoroutines")

    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))

    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("androidx.core:core-ktx:1.5.0")
    implementation("com.github.moxy-community:moxy:$verMoxy")
    implementation("com.squareup.retrofit2:retrofit:$verRetrofit")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$verCoroutines")
}