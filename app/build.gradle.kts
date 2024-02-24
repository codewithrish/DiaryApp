@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    // KSP
    alias(libs.plugins.ksp)
    // Kapt
    // alias(libs.plugins.kotlinKapt)
    // Realm
    alias(libs.plugins.realmKotlin)
    // Hilt
    alias(libs.plugins.daggerHilt)
    // alias(libs.plugins.googleServices)
}

android {
    namespace = "com.codewithrish.diaryapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.codewithrish.diaryapp"
        minSdk = 21
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)

    // Dagger Hilt
    implementation(libs.dagger.hilt)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.hilt.navigation)
    // Compose Navigation
    implementation(libs.compose.navigation)
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    // Room components
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    // Runtime Compose
    implementation(libs.compose.runtime)
    // Splash API
    implementation(libs.splash)
    // MongoDB Realm
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.realm.sync)
    // Coil
    implementation(libs.coil)
    // Pager - Accompanist [DEPRECATED]
    implementation(libs.pager)
    // Date-Time Picker
    implementation(libs.date.time.picker)
    // CALENDAR
    implementation(libs.calender)
    // CLOCK
    implementation(libs.clock)
    // Message Bar Compose
    implementation(libs.message.bar.compose)
    // One-Tap Compose
    implementation(libs.one.tap.compose)
    // Desugar JDK
    coreLibraryDesugaring(libs.desugar.jdk)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}