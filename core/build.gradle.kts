
plugins {
    application
    id("net.kyori.blossom") version "2.1.0"
    id("com.gradleup.shadow") version "9.4.1"
}

group = rootProject.group;
version = rootProject.version;

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":api"))
    compileOnly("ch.qos.logback:logback-classic:1.5.6") // logging
}

application {
    mainClass = "net.survivalboom.sbds.core.Main"
}



tasks {

    shadowJar {

        archiveBaseName = "SBDS"
        archiveVersion = rootProject.version.toString()
        archiveClassifier = ""

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
