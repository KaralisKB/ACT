plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services") //  Firebase initialization
}

android {
    namespace = "com.example.act_mobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.act_mobile"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "RAPIDAPI_KEY", "\"ca048bfadcmsh7163d2b1c2a7605p17a2fdjsnd147747d629b\"")

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
    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
dependencies {
    // Core dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Material3 (use a specific version to avoid duplication)
    implementation("androidx.compose.material3:material3:1.1.0") // Replace with the latest stable version

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.5.0"))
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.firebase:firebase-firestore:25.1.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-functions-ktx")

    // Networking dependencies (Retrofit)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Compose and UI dependencies
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.material)
    implementation(libs.compose.ui.tooling)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation("androidx.compose.foundation:foundation:1.5.3")
    implementation("com.google.android.material:material:1.12.0")

    // Permissions and image loading
    implementation("com.google.accompanist:accompanist-permissions:0.24.10-beta")
    implementation("io.coil-kt:coil-compose:2.0.0")

    // Chart library and Stripe
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.stripe:stripe-android:21.0.0")
    implementation("com.stripe:stripeterminal:3.10.0")

    // Polygon.io library for stock data
    implementation("com.github.polygon-io:client-jvm:v5.1.2")
    implementation(libs.volley)

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.paypal.android:card-payments:1.3.0")
}


apply(plugin = "com.google.gms.google-services")
