plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.cne_commute"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cne_commute"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        jvmTarget = "11"  // Ensure Kotlin compatibility with Java 11
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat.v141)
    implementation(libs.material.v150)
    implementation(libs.activity.ktx)
    implementation(libs.constraintlayout.v213)
    implementation(libs.firebase.database)
    implementation(libs.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.v113)
    androidTestImplementation(libs.espresso.core.v340)

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))  // Firebase BOM (Bill of Materials)
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")  // Firebase Authentication
    implementation("com.google.firebase:firebase-firestore")  // Firebase Firestore

    // ZXing library for QR code scanning
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.android.material:material:1.9.0")

    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")

    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")



}
