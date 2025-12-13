plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":api"))
    compileOnly("net.dv8tion:JDA:6.1.2")
    compileOnly("org.bspfsystems:yamlconfiguration:2.0.1")
}