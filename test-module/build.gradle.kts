plugins {
    java
}

group = "net.survivalboom.sbds.modules.test"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":api"))
    compileOnly("net.dv8tion:JDA:5.2.2")
    compileOnly("org.bspfsystems:yamlconfiguration:2.0.1")
}