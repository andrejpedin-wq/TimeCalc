plugins {
    id("com.android.application")
}

android {
    namespace = "com.timecalc"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.timecalc"
        minSdk = 21
        targetSdk = 34
        versionCode = 4
        versionName = "4.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file("release.keystore")
            storePassword = System.getenv("KEYSTORE_PASS") ?: "11q22w33e44r"
            keyAlias = "TimeCalc"
            keyPassword = System.getenv("KEYSTORE_PASS") ?: "11q22w33e44r"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
