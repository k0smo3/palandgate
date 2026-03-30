plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "21"
}

application {
    mainClass.set("me.csystems.gategarage.cli.MainKt")
}

dependencies {
    implementation(project(":core"))
    implementation(libs.gson)
}
