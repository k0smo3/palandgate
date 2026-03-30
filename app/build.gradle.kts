import java.util.Date
import java.text.SimpleDateFormat

fun gitCommitCount(): Int =
    Runtime.getRuntime().exec(arrayOf("git", "rev-list", "--count", "HEAD"))
        .inputStream.bufferedReader().readText().trim().toIntOrNull() ?: 1

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

base.archivesName.set("palandgate")

android {
    namespace = "me.csystems.palandgate"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "me.csystems.palandgate"
        minSdk = 26
        targetSdk = 35
        versionCode = gitCommitCount()
        versionName = "1.0"
        buildConfigField("String", "BUILD_DATE", "\"${SimpleDateFormat("yyyy-MM-dd").format(Date())}\"")
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.material)
    implementation(libs.coroutines.android)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.security.crypto)
    implementation(libs.swiperefreshlayout)

    testImplementation(libs.junit)
}
