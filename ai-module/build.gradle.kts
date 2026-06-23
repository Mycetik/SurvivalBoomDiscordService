plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
}

group = "net.survivalboom.sbds.modules.ai"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":api"))
    compileOnly("io.github.sashirestela:simple-openai:3.22.2")
}