plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {

    compileOnlyApi("org.jetbrains:annotations:15.0")

    compileOnlyApi("net.dv8tion:JDA:6.4.1")
    compileOnlyApi("org.hibernate.orm:hibernate-core:6.6.9.Final")

    compileOnlyApi("org.spongepowered:configurate-yaml:4.2.0")
    compileOnlyApi("org.spongepowered:configurate-xml:4.2.0")
    compileOnlyApi("org.spongepowered:configurate-jackson:4.2.0")

    compileOnlyApi("com.fasterxml.jackson.core:jackson-core:2.18.3")
    compileOnlyApi("com.fasterxml.jackson.core:jackson-databind:2.18.3")

}