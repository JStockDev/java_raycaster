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
    implementation("com.moandjiezana.toml:toml4j:0.7.2")
    implementation("org.java-websocket:Java-WebSocket:1.6.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "dev.jstock.server.Entry"
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to application.mainClass,
        )
    }
}