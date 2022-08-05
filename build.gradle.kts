plugins {
    id("org.jetbrains.intellij") version "1.8.0"
    id("org.jetbrains.kotlin.jvm") version "1.7.0"
}

group = "me.oddlyoko"
version = "0.0.4"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

intellij {
    version.set("2022.2")
    type.set("PC")
    plugins.set(listOf(
        "PythonCore",
    ))
}

tasks {
    patchPluginXml {
        sinceBuild.set("222.3345.131")
        untilBuild.set("222.*")
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}
