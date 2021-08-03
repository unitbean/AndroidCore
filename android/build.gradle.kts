import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.library")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
    kotlin("android")
}

tasks.dokkaJavadoc.configure {
    outputDirectory.set(buildDir.resolve("javadoc"))
}

mavenPublish {
    sonatypeHost = com.vanniktech.maven.publish.SonatypeHost.S01
}

android {
    compileSdk = 30
    defaultConfig {
        minSdk = 16
        targetSdk = 30
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
val verCoroutines = "1.5.1"
val verRetrofit = "2.9.0"

dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    testImplementation("org.mockito:mockito-core:3.11.2")
    testImplementation("org.assertj:assertj-core:3.20.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$verCoroutines")

    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))

    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("com.github.moxy-community:moxy:$verMoxy")
    implementation("com.squareup.retrofit2:retrofit:$verRetrofit")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$verCoroutines")
}