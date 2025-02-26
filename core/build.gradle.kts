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

    implementation(project(":api"))

    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("net.dv8tion:JDA:5.2.2")

    // YAML CONFIGURATION
    implementation("org.bspfsystems:yamlconfiguration:2.0.1")

    // ANNOTATIONS
    implementation("org.jetbrains:annotations:15.0")

    // HIKARI
    implementation("com.zaxxer:HikariCP:6.2.1")

    // JSON
    implementation("org.json:json:20240303")

    //
    // HIBERNATE
    //

    // HIBERNATE CORE
    implementation("org.hibernate.orm:hibernate-core:6.6.9.Final")

    // Hibernate Community Dialects
    // https://mvnrepository.com/artifact/org.hibernate.orm/hibernate-community-dialects
    implementation("org.hibernate.orm:hibernate-community-dialects:6.6.9.Final")

    //
    // DB DRIVERS
    //

    // POSTRGRE SQL DRIVER
    implementation("org.postgresql:postgresql:42.7.5")

    // MYSQL DRIVER
    // https://mvnrepository.com/artifact/com.mysql/mysql-connector-j
    implementation("com.mysql:mysql-connector-j:9.2.0")

    // SQLITE DRIVER
    // https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")


}

application {
    mainClass = "net.survivalboom.sbds.core.Main"
}

val outFile = File(rootProject.projectDir, "SBDS-${version}.jar")
val runDir = File(rootProject.projectDir, "run")
val runFile = File(runDir, outFile.name)

tasks {

    shadowJar {

        archiveBaseName.set("SBDS")
        archiveVersion.set(rootProject.version.toString())
        archiveClassifier.set("")

        destinationDirectory.set(rootProject.rootDir)

    }

    build {
        dependsOn(shadowJar)
    }

    clean {

        doLast {
            Files.deleteIfExists(runFile.toPath())
        }

    }

    val copyToRun = create("copyToRun") {

        dependsOn(shadowJar)

        doFirst {

            Files.deleteIfExists(runFile.toPath())
            Files.copy(outFile.toPath(), runFile.toPath())

        }

    }

    create<Exec>("runApp") {

        dependsOn(copyToRun)
        commandLine("java", "-jar", runFile.path)

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