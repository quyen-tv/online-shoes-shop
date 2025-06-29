plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.prm392.onlineshoesshop"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.prm392.onlineshoesshop"
        minSdk = 24
        targetSdk = 35
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
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.firebase.database)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.chip.navigation.bar)
    implementation(libs.viewpager2)
    implementation(libs.glide)
    implementation(libs.firebase.bom)
    implementation(libs.google.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation (libs.dotsindicator)
    implementation(libs.gson)
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    // To use constraintlayout in compose
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")
    // OkHttp client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // (Tùy chọn) Logging Interceptor để debug request/response
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}