import org.jetbrains.kotlin.config.KotlinCompilerVersion
import com.vanniktech.maven.publish.SonatypeHost
import Common.getLocalProperty

plugins {
    id("com.android.library")
    id("com.vanniktech.maven.publish")
    kotlin("android")
}

tasks.dokkaJavadoc.configure {
    outputDirectory.set(buildDir.resolve("javadoc"))
}

mavenPublishing {
    coordinates(
        groupId = getLocalProperty(key = "GROUP"),
        artifactId = "androidcore",
        version = getLocalProperty(key = "VERSION_NAME_CORE")
    )
    signAllPublications()
    publishToMavenCentral(SonatypeHost.S01)
}

android {
    namespace = "com.ub.ubutils"
    compileSdk = 33
    defaultConfig {
        minSdk = 16
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        buildConfig = false
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("org.mockito:mockito-core:5.3.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Deps.coroutinesVer}")

    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))

    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Deps.lifecycleVer}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Deps.lifecycleVer}")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.exifinterface:exifinterface:1.3.6")
    implementation("com.github.moxy-community:moxy:${Deps.moxyVer}")
    implementation("com.squareup.retrofit2:retrofit:${Deps.retrofitVer}")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Deps.coroutinesVer}")
}