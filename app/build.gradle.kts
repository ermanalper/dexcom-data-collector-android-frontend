import java.util.Properties

// local.properties dosyasını bul ve oku
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("kapt")
    alias(libs.plugins.hilt)
}
hilt {
    enableAggregatingTask = false
}
android {
    namespace = "com.alptrosoft.dexcom_data_collector_android_frontend"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.alptrosoft.dexcom_data_collector_android_frontend"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        debug {
            val localUrl = localProperties.getProperty("DNS_ADRESS")
                ?: "\"http://10.0.2.2:8080/\""

            val apiKey = localProperties.getProperty("API_KEY")
                ?: "\"\""
            val clientName = localProperties.getProperty("CLIENT_NAME")
                ?: "\"UNKNOWN-ANDROID-CLIENT\""

            buildConfigField("String", "BASE_URL", localUrl)
            buildConfigField("String", "API_KEY", apiKey)
            buildConfigField("String", "CLIENT_NAME", clientName)
        }

        release {
            val localUrl = localProperties.getProperty("DNS_ADRESS")
                ?: "\"http://10.0.2.2:8080/\""

            val apiKey = localProperties.getProperty("API_KEY")
                ?: "\"\""
            val clientName = localProperties.getProperty("CLIENT_NAME")
                ?: "\"UNKNOWN-ANDROID-CLIENT\""

            buildConfigField("String", "BASE_URL", localUrl)
            buildConfigField("String", "API_KEY", apiKey)
            buildConfigField("String", "CLIENT_NAME", clientName)
            isMinifyEnabled = true
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
}

dependencies {
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(platform("androidx.compose:compose-bom:2025.08.00"))
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.hilt.android)
    implementation(libs.logging.interceptor)
    implementation(libs.javax.inject)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.contentpager)
    implementation(libs.androidx.foundation.android)
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    add("kapt", libs.hilt.compiler)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
}
