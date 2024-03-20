plugins {
    id("java")
}

allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    group = "io.github.bitterfox"
    version = "0.21.0-SNAPSHOT"

    dependencies {
        testImplementation(platform("org.junit:junit-bom:5.9.1"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.toVersion("21")
        targetCompatibility = JavaVersion.toVersion("21")
    }

    tasks.test {
        useJUnitPlatform()
        jvmArgs("--enable-preview")
    }

    tasks.compileJava {
        options.compilerArgs.add("--enable-preview")
    }
    tasks.compileTestJava {
        options.compilerArgs.add("--enable-preview")
    }
}
