plugins {
    java
}

group = "net.survivalboom.sbds.modules.music"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":api"))
    compileOnly("dev.arbjerg:lavalink-client:3.2.0")
}