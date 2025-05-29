plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit)
    implementation(libs.guava)

    
    implementation(project(":commons"))
    implementation("com.googlecode.lanterna:lanterna:3.2.0-alpha1")
    implementation("org.java-websocket:Java-WebSocket:1.6.0")
    implementation("com.moandjiezana.toml:toml4j:0.7.2")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "dev.jstock.client.Entry"

    tasks.run.get().workingDir = rootProject.projectDir
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to application.mainClass,
        )
    }
}