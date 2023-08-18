import com.vanniktech.maven.publish.SonatypeHost
import Common.getLocalProperty

plugins {
    id("com.android.library")
    id("com.vanniktech.maven.publish")
    kotlin("android")
    id("kotlin-parcelize")
}

mavenPublishing {
    coordinates(
        groupId = getLocalProperty(key = "GROUP"),
        artifactId = project.name,
        version = getLocalProperty(key = "VERSION_NAME_YANDEX")
    )
    signAllPublications()
    publishToMavenCentral(SonatypeHost.S01)
}

tasks.dokkaJavadoc.configure {
    outputDirectory.set(layout.buildDirectory.dir("javadoc"))
}

android {
    namespace = "com.ub.yandex"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt")
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt")
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
}

dependencies {
    api("com.yandex.android:maps.mobile:4.3.2-lite")
    implementation("androidx.fragment:fragment-ktx:${Deps.fragmentVer}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Deps.lifecycleVer}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Deps.lifecycleVer}")
}