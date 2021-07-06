import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.72"
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.9"
    id("com.github.ben-manes.versions") version "0.28.0"
    id("org.jetbrains.dokka") version "1.4.0-rc"
}


group = "de.jonasnick.antnet"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.4")

    testCompile("junit", "junit", "4.13")

    // https://mvnrepository.com/artifact/com.google.guava/guava
    implementation("com.google.guava", "guava", "29.0-jre")

    implementation("no.tornado", "tornadofx", "2.0.0-SNAPSHOT")
    // https://mvnrepository.com/artifact/org.graphstream/gs-core
    implementation("org.graphstream", "gs-core", "1.3")
    // https://mvnrepository.com/artifact/org.graphstream/gs-algo
    implementation("org.graphstream", "gs-algo", "1.3")

    // https://mvnrepository.com/artifact/org.graphstream/gs-ui
    implementation("org.graphstream", "gs-ui", "1.3")
    // https://mvnrepository.com/artifact/org.knowm.xchart/xchart
    implementation("org.knowm.xchart", "xchart", "3.6.4")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

javafx {
    version = "12.0.1"
    modules("javafx.controls", "javafx.fxml", "javafx.swing")
}