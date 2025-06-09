import java.nio.file.Files

plugins {
    application
    id("net.kyori.blossom") version("2.1.0")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "net.survivalboom.sbds.core"
version = rootProject.version

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":api"))
    compileOnly("ch.qos.logback:logback-classic:1.5.6") // logging
}

application {
    mainClass = "net.survivalboom.sbds.core.Main"
}



tasks {

    shadowJar {

        dependsOn(":api:jar")

        archiveBaseName.set("SBDS")
        archiveVersion.set(rootProject.version.toString())
        archiveClassifier.set("")

        from(zipTree(project(":api").tasks.jar.get().archiveFile.get()))

    }

    build {
        dependsOn(shadowJar)
    }

}


sourceSets {
    main {
        blossom {
            javaSources {
                property("version", project.version.toString())
            }
        }
    }
}
