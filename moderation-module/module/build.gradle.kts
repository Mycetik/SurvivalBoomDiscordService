
plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
}

group = "net.survivalboom.sbds.moderation"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(project(":moderation-module:api"))
}


tasks {

    shadowJar {

        dependsOn(":moderation-module:api:jar")
        from(zipTree(project(":moderation-module:api").tasks.jar.get().archiveFile.get()))

        archiveFileName = "${parent?.name}-${version}.jar"

    }

    build {
        dependsOn(shadowJar)
    }

}