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
    implementation("org.json:json:20240303")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
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
