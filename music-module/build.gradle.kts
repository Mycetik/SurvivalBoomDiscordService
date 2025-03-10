plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":api"))
    compileOnly("dev.arbjerg:lavalink-client:3.2.0")
}