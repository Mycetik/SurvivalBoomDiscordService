plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {

    api("net.dv8tion:JDA:6.4.0")
    api("org.bspfsystems:yamlconfiguration:2.0.1")
    api("org.hibernate.orm:hibernate-core:6.6.9.Final")
    api("org.json:json:20240303")
    api("org.jetbrains:annotations:15.0")

    api("com.fasterxml.jackson.core:jackson-core:2.18.3")
    api("com.fasterxml.jackson.core:jackson-databind:2.18.3")

}