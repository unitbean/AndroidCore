import org.jetbrains.kotlin.config.KotlinCompilerVersion
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
    kotlin("android")
}

tasks.dokkaJavadoc.configure {
    outputDirectory.set(buildDir.resolve("javadoc"))
}

mavenPublishing {
    signAllPublications()
    pomFromGradleProperties()
    publishToMavenCentral(SonatypeHost.S01)
}

android {
    namespace = "com.ub.ubutils"
    compileSdk = 33
    defaultConfig {
        minSdk = 16
        targetSdk = 33
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
val verCoroutines = "1.6.4"
val verRetrofit = "2.9.0"

dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    testImplementation("org.mockito:mockito-core:4.8.1")
    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$verCoroutines")

    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))

    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("com.github.moxy-community:moxy:$verMoxy")
    implementation("com.squareup.retrofit2:retrofit:$verRetrofit")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$verCoroutines")
}