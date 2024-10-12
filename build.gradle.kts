plugins {
    kotlin("jvm") version "1.8.10" apply false
    alias(libs.plugins.android.application) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}