plugins {
    java
}

group = "net.survivalboom.modules.chatbot"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("com.openai:openai-java:2.5.0")
    compileOnly(project(":api"))
}