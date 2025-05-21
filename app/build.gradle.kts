plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.cne_commute"
    compileSdk = 35

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
    // Use Firebase BoM for version management
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:23.1.0") // Ensure latest compatible
    implementation("com.google.firebase:firebase-firestore")

    // Updated Material Design library
    implementation("com.google.android.material:material:1.12.0")

    // Updated androidx libraries
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")

    // Google Play Services
    implementation("com.google.android.gms:play-services-maps:19.0.0")  // Latest version
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // ZXing QR Code scanning
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    implementation("androidx.cardview:cardview:1.0.0")

    // Testing libraries
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
