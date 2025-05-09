plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit)
    implementation(libs.guava)

    implementation("io.github.libsdl4j:libsdl4j:2.28.4-1.6")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "dev.jstock.Entry"
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to application.mainClass,
        )
    }
}