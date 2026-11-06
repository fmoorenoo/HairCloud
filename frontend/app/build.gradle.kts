import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// La URL de la API se configura fuera del código (local.properties o -P en CI)
// en lugar de editar una constante en ApiClient.kt antes de cada build.
val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun apiUrl(buildType: String, default: String): String {
    val url = (project.findProperty("haircloud.apiBaseUrl.$buildType") as String?)
        ?: localProps.getProperty("haircloud.apiBaseUrl.$buildType")
        ?: default
    return if (url.endsWith("/")) url else "$url/"
}

android {
    namespace = "com.haircloud"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.haircloud"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL",
                "\"${apiUrl("debug", "http://10.0.2.2:5000/api/v1/")}\"")
        }
        release {
            val releaseUrl = apiUrl("release", "https://api.haircloud.example/api/v1/")
            require(releaseUrl.startsWith("https://")) {
                "haircloud.apiBaseUrl.release debe usar HTTPS"
            }
            buildConfigField("String", "API_BASE_URL", "\"$releaseUrl\"")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // Coil
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.9")

    // Icons
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // Red: Retrofit + OkHttp (versiones alineadas; antes se mezclaba OkHttp 3 y 4)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.3")

    // Coroutines (antes core 1.6.4 y android 1.7.3: versiones incompatibles)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
