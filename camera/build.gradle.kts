plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.ub.camera"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
    packaging {
        resources.excludes += "DebugProbesKt.bin"
    }
}

val cameraxVer = "1.2.3"

dependencies {

    api("androidx.camera:camera-core:${cameraxVer}")
    api("androidx.camera:camera-view:${cameraxVer}")
    implementation("androidx.camera:camera-camera2:${cameraxVer}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVer}")

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Deps.coroutinesVer}")
    implementation("androidx.concurrent:concurrent-futures-ktx:1.1.0")
}