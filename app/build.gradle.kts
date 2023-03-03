import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    namespace = "com.ub.utils"
    compileSdk = 33
    defaultConfig {
        applicationId = "com.ub.utils"
        minSdk = 16
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

val verMoxy = "2.2.2"
val verDagger = "2.45"
val verRetrofit = "2.9.0"
val verCoroutines = "1.6.4"
val verLifecycle = "2.5.1"

dependencies {
    implementation(project(":android"))
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))

    // testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // kapts
    kapt("com.github.moxy-community:moxy-compiler:$verMoxy")
    kapt("com.google.dagger:dagger-compiler:$verDagger")

    // android x
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.vectordrawable:vectordrawable:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.6.1")
    implementation("androidx.fragment:fragment-ktx:1.5.5")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.biometric:biometric:1.1.0")

    // moxy
    implementation("com.github.moxy-community:moxy:$verMoxy")
    implementation("com.github.moxy-community:moxy-androidx:$verMoxy")
    implementation("com.github.moxy-community:moxy-material:$verMoxy")
    implementation("com.github.moxy-community:moxy-ktx:$verMoxy")

    // lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$verLifecycle")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$verLifecycle")

    // retrofit 2
    implementation("com.squareup.retrofit2:retrofit:$verRetrofit")
    implementation("com.squareup.retrofit2:converter-gson:$verRetrofit")
    implementation("com.squareup.retrofit2:adapter-rxjava2:$verRetrofit")

    // logging interceptor
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // rx android
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")

    // dagger 2
    implementation("com.google.dagger:dagger:$verDagger")

    // kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$verCoroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$verCoroutines")
}
