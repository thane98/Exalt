import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "1.4.31"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("org.beryx.runtime") version "1.12.2"
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.10"
}

val compileKotlin: KotlinCompile by tasks
val compileJava: JavaCompile by tasks
compileJava.destinationDir = compileKotlin.destinationDir

javafx {
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("com.thane98.exalt.ui.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.31")
    implementation("com.xenomachina", "kotlin-argparser", "2.0.7")
    implementation("org.fxmisc.richtext:richtextfx:0.10.6")
    implementation("org.jfxtras:jmetro:11.6.15")
    implementation(platform("org.http4k:http4k-bom:4.14.1.4"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-netty")
}

runtime {
    launcher {
        noConsole = true
    }
    imageZip.set(project.file("${project.buildDir}/image-zip/exalt-image.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    modules.set(listOf("java.desktop", "jdk.unsupported", "java.scripting", "java.logging", "java.xml"))
}
