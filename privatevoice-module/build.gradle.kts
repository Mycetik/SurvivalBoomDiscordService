plugins {
    java
}

group = "net.survivalboom.modules.voices"
version = "2.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":api"))
}