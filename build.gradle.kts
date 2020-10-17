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

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_14
}
