import com.vanniktech.maven.publish.SonatypeHost
import Common.getLocalProperty

plugins {
    id("com.android.library")
    id("com.vanniktech.maven.publish")
    kotlin("android")
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
    outputDirectory.set(buildDir.resolve("javadoc"))
}

android {
    namespace = "com.ub.yandex"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt")
            )
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt")
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildFeatures {
        buildConfig = false
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.yandex.android:maps.mobile:4.3.1-lite")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}