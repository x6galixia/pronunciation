plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "com.example.pronunciationchecker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pronunciationchecker"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildFeatures {
            buildConfig = true
        }

        buildConfigField("String", "HF_API_TOKEN", "\"${project.findProperty("HF_API_TOKEN") ?: "default_token"}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "HF_API_TOKEN", "\"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\"")
        }
        release {
            buildConfigField("String", "HF_API_TOKEN", "\"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\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.10")
    implementation("com.google.mlkit:language-id:17.0.6")  // or 17.0.8
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}