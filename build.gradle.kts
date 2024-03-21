import net.thebugmc.gradle.sonatypepublisher.PublishingType.USER_MANAGED

plugins {
    id("java")
    id("java-library")
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.3"
}

allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    group = "io.github.bitterfox"
    // 0.<java version>.<minor version>
    version = "0.21.3"

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

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "net.thebugmc.gradle.sonatype-central-portal-publisher")

    signing {
        useGpgCmd()
    }

    centralPortal {
        publishingType = USER_MANAGED

        name = project.name
        description = "JSON String Template Libraries for Java's String Template Language Feature (Java 21, Preview)"

        pom {
            url = "https://github.com/bitterfox/json-string-template"
            licenses {
                license {
                    name = "Apache License, Version 2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0"
                }
            }
            developers {
                developer {
                    name = "bitter_fox"
                }
            }
            scm {
                url = "https://github.com/bitterfox/json-string-template"
                connection = "scm:git:git://github.com/bitterfox/json-string-template.git"
                developerConnection = "scm:git:git@github.com:bitterfox/json-string-template.git"
            }
        }

        jarTask = tasks.jar

        sourcesJarTask = tasks.create<Jar>("sourcesEmptyJar") {
            archiveClassifier = "sources"
        }
        javadocJarTask = tasks.create<Jar>("javadocEmptyJar") {
            archiveClassifier = "javadoc"
        }
    }
}
