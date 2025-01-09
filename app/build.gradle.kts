plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}



android {
    namespace = "com.yarmouk.lena"
    compileSdk = 35
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.yarmouk.lena"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        buildConfigField("String", "GOOGLE_API_KEY", "\"${project.findProperty("GOOGLE_API_KEY")}\"")
        buildConfigField("String", "WIT_AI_TOKEN", "\"${project.findProperty("WIT_AI_TOKEN")}\"")
        buildConfigField("String", "OPEN_WEATHER_MAP_API_KEY", "\"${project.findProperty("OPEN_WEATHER_MAP_API_KEY")}\"")


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
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    val nav_version = "2.8.3"
    val ai_version = "0.9.0"
    val compose_version = "1.7.5"
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.compose.runtime:runtime-livedata:$compose_version")
    implementation("com.google.ai.client.generativeai:generativeai:$ai_version")
    implementation("androidx.navigation:navigation-compose:$nav_version")
    implementation ("androidx.compose.material:material-icons-extended:$compose_version")
    implementation ("androidx.core:core-splashscreen:1.0.1")
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.google.accompanist:accompanist-permissions:0.24.13-rc")
}



