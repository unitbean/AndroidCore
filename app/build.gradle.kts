import Common.getLocalProperty
import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.ub.utils"
    compileSdk = 33
    defaultConfig {
        applicationId = "com.ub.utils"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "YANDEX_KEY", "\"${getLocalProperty(key = "yandex.key", file = "local.properties")}\"")
        }

        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "YANDEX_KEY", "\"${getLocalProperty(key = "yandex.key", file = "local.properties")}\"")
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
        viewBinding = true
    }
    packaging {
        resources.excludes += "DebugProbesKt.bin"
    }
}

dependencies {
    implementation(project(":android"))
    implementation(project(":yandex"))
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))

    // testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // android x
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.vectordrawable:vectordrawable:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.fragment:fragment-ktx:${Deps.fragmentVer}")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.biometric:biometric:1.1.0")

    // lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Deps.lifecycleVer}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Deps.lifecycleVer}")

    // retrofit 2
    implementation("com.squareup.retrofit2:retrofit:${Deps.retrofitVer}")
    implementation("com.squareup.retrofit2:converter-gson:${Deps.retrofitVer}")
    implementation("com.squareup.retrofit2:adapter-rxjava2:${Deps.retrofitVer}")

    // logging interceptor
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // rx android
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")

    // Kotlin Inject
    ksp("me.tatarka.inject:kotlin-inject-compiler-ksp:${Deps.kotlinInjectVer}")
    implementation("me.tatarka.inject:kotlin-inject-runtime:${Deps.kotlinInjectVer}")

    // kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Deps.coroutinesVer}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Deps.coroutinesVer}")

    implementation("dev.chrisbanes.insetter:insetter:0.6.1")
    implementation("com.jakewharton.timber:timber:5.0.1")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}
