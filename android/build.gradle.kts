import com.vanniktech.maven.publish.SonatypeHost
import Common.getLocalProperty

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kotlin.android)
}

tasks.dokkaJavadoc.configure {
    outputDirectory.set(layout.buildDirectory.dir("javadoc"))
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
    compileSdk = 34
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
    packaging {
        resources.excludes += "DebugProbesKt.bin"
    }
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.assertj.core)
    testImplementation(libs.kotlinx.coroutines.android)

    implementation(libs.kotlin)

    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.exifinterface)
    implementation(libs.moxy)
    implementation(libs.retrofit)
    implementation(libs.kotlinx.coroutines.core)
}