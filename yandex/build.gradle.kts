import com.vanniktech.maven.publish.SonatypeHost
import Common.getLocalProperty

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kotlin.android)
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
    compileSdk = 34

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
    api(libs.maps.mobile)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}