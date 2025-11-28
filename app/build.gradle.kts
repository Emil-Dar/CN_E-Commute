plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") //  Firebase plugin
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
        debug {
            buildConfigField(
                "String",
                "SUPABASE_API_KEY",
                "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJ0d3Jia3Jyb2lsZnRkaGdneGpjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQ4MDg1OTksImV4cCI6MjA3MDM4NDU5OX0.eiCQTeLh9IG4mX3cNqoIe6-cq33pzeO_qSTtONuMnKA\""
            )
        }
        release {
            buildConfigField(
                "String",
                "SUPABASE_API_KEY",
                "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJ0d3Jia3Jyb2lsZnRkaGdneGpjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQ4MDg1OTksImV4cCI6MjA3MDM4NDU5OX0.eiCQTeLh9IG4mX3cNqoIe6-cq33pzeO_qSTtONuMnKA\""
            )
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
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    //  Firebase BOM and Authentication
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    //  Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // AndroidX Core
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")

    // Material Design
    implementation("com.google.android.material:material:1.12.0")

    // Google Maps & Location
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // QR Scanner
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("com.google.android.material:material:1.12.0")

    // JSON Handling
    implementation("org.json:json:20231013")

    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.11.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0")

    // File & Stream Utilities
    implementation("commons-io:commons-io:2.11.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    // ViewPager and UI
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.code.gson:gson:2.10.1")
<<<<<<< HEAD

    // Supabase
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.2") // core + postgrest
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.0.2") // for auth if needed
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // bcrypt library for Java/Android
    implementation("at.favre.lib:bcrypt:0.9.0")

=======
>>>>>>> Krizza
}
