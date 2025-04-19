plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization") version "1.6.0"
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.fizyoapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fizyoapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Temel Android bağımlılıkları
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose bağımlılıkları
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.dynamic.features.fragment)

    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    implementation(libs.firebase.storage.ktx)
    kapt(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Animasyon
    implementation("com.airbnb.android:lottie-compose:6.4.1")

    // Material Icons
    implementation("androidx.compose.material:material-icons-extended:1.7.6")

    // Lifecycle Components
    val lifecycleVersion = "2.8.7"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-service:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-reactivestreams-ktx:$lifecycleVersion")

    // Hilt Bağımlılıkları
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-navigation-fragment:1.0.0")
    implementation("androidx.hilt:hilt-work:1.0.0")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    // Firebase Bağımlılıkları (tekrar önlemek için sadece bir kez eklendi)
    implementation(platform("com.google.firebase:firebase-bom:33.11.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage") // Profil fotoğrafları için

    // Diğer Bağımlılıklar
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Test Bağımlılıkları
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(libs.androidx.navigation.testing)
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.lifecycle:lifecycle-runtime-testing:$lifecycleVersion")

    implementation("androidx.work:work-runtime-ktx:2.9.0")

    implementation("androidx.compose.material3:material3:1.1.2")

    // Resim yükleme
    implementation("io.coil-kt:coil-compose:2.4.0")

    // İzin yönetimi
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    implementation ("io.coil-kt:coil-compose:2.4.0")


}

kapt {
    correctErrorTypes = true
}