plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.shashwat.memolens"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.shashwat.memolens"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.3"
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

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java", "src/main/java/com/shashwat/adapters")
        }
    }

    buildFeatures {
        viewBinding = true // If you plan to use View Binding
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // CameraX Dependencies
    implementation("androidx.camera:camera-camera2:1.1.0")
    implementation("androidx.camera:camera-lifecycle:1.1.0")
    implementation("androidx.camera:camera-view:1.1.0")

    // Image Libraries
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ExifInterface for image metadata
    implementation("androidx.exifinterface:exifinterface:1.3.6")

    // Image Steganography Library
    implementation(project(":ImageSteganographyLibrary"))

    // Colour Palette background
    implementation ("androidx.palette:palette:1.0.0")

}
