plugins {
    java
}

group = "net.survivalboom.sbds.modules.voice"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":api"))
}