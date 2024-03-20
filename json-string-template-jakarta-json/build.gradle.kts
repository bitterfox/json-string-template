plugins {
    id("java")
}

group = "com.github.bitterfox"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":json-string-template-base"))
    implementation("jakarta.json:jakarta.json-api:2.1.3")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.eclipse.parsson:parsson:1.1.5")
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
