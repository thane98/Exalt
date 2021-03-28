import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "1.4.31"
    application
    id("org.openjfx.javafxplugin") version "0.0.9"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("org.beryx.runtime") version "1.12.2"
}

val compileKotlin: KotlinCompile by tasks
val compileJava: JavaCompile by tasks
compileJava.destinationDir = compileKotlin.destinationDir

application {
    mainClassName = "com.thane98.exalt.editor.Launcher"
}

repositories {
    mavenCentral()
}

javafx {
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.31")
    implementation("com.xenomachina", "kotlin-argparser", "2.0.7")
    implementation("org.fxmisc.richtext", "richtextfx", "0.10.6")
    implementation("org.jfxtras:jmetro:11.6.14")
}

runtime {
    launcher {
        noConsole = true
    }
    imageZip.set(project.file("${project.buildDir}/image-zip/exalt-image.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    modules.set(listOf("java.desktop", "jdk.unsupported", "java.scripting", "java.logging", "java.xml"))
}