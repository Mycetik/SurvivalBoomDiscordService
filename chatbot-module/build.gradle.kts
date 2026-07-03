plugins {
    java
}

group = "net.survivalboom.sbds.modules.chatbot"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.github.sashirestela:simple-openai:3.22.2")
    compileOnly(project(":api"))
    compileOnly(project(":moderation-module:api"))
    compileOnly(project(":ai-module"))
}