import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.30"
    application
}

group = "me.corrot"
version = "1.0-SNAPSHOT"

val lwjglVersion = "3.2.3"
val lwjglNatives = "natives-linux"

application {
    mainClassName = "MainKt"
}

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.lwjgl", "lwjgl-stb")
//    implementation("org.lwjgl", "lwjgl-assimp")
//    implementation("org.lwjgl", "lwjgl-openal")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives)
//    runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = lwjglNatives)
//    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
}
