
plugins {
    java
}

group = "net.survivalboom.sbds.modules.translation"
version = "1.1"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":api"))
}