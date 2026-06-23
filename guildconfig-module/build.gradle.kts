plugins {
    java
}

group = "net.survivalboom.sbds.modules.guildconfig"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":api"))
}
