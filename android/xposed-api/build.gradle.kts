import java.util.Properties

plugins {
    `java-library`
}

val sdkDir = Properties().apply {
    val localProperties = rootProject.file("local.properties")
    if (localProperties.exists()) localProperties.inputStream().use(::load)
}.getProperty("sdk.dir") ?: System.getenv("ANDROID_HOME")

require(!sdkDir.isNullOrBlank()) { "Android SDK path is required for the Xposed API stub" }

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(files("$sdkDir/platforms/android-37.0/android.jar"))
}
