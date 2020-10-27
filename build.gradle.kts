plugins {
    java
}

group = "com.github.igorperikov.botd"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.google.api-client", "google-api-client", "1.30.11")
    implementation("com.google.apis", "google-api-services-sheets", "v4-rev612-1.25.0")

    implementation("org.apache.commons", "commons-lang3", "3.11")
    implementation("org.apache.commons", "commons-text", "1.9")

    implementation("ch.qos.logback", "logback-core", "1.2.3")
    implementation("ch.qos.logback", "logback-classic", "1.2.3")
    implementation("org.slf4j", "slf4j-api", "1.7.30")

    implementation("se.michaelthelin.spotify", "spotify-web-api-java", "6.4.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_14
}
