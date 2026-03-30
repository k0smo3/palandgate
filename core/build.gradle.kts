plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "me.csystems.gategarage"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "21"
}

dependencies {
    api(libs.okhttp)
    implementation(libs.okhttp.logging)

    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
}
