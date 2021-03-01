import org.jenkinsci.gradle.plugins.jpi.JpiDeveloper
import org.jenkinsci.gradle.plugins.jpi.JpiLicense
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.31"
    kotlin("kapt") version "1.4.31"
    id("org.jenkins-ci.jpi") version "0.42.0"
}

group = "com.gihub.findcoo.jenkins.plugin"
version = "0.0.1"


repositories {
    jcenter()
}

jenkinsPlugin {
    jenkinsVersion.set("2.280")
    displayName = "SCDF Client Plugin"
    shortName = "scdf-client-plugin"
    gitHubUrl = "https://github.com/findcoo/scdf-client-plugin"
    repoUrl = "https://repo.jenkins-ci.org/releasese"
    snapshotRepoUrl = "https://repo.jenkins-ci.org/snapshots"
    pluginFirstClassLoader = true
    developers = this.Developers().apply {
        developer(delegateClosureOf<JpiDeveloper> {
            setProperty("id", "findcoo")
            setProperty("name", "Ui seong")
            setProperty("email", "thirdlif2@gmail.com")
        })
    }
    licenses = this.Licenses().apply {
        license(delegateClosureOf<JpiLicense> {
            setProperty("url", "http://www.apache.org/licenses/LICENSE-2.0")
        })
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8", "1.4.31"))
    implementation(kotlin("reflect", "1.4.31"))

    implementation("org.springframework.cloud:spring-cloud-dataflow-rest-client:2.7.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.3")
    implementation("org.jenkins-ci.plugins:credentials:2.3.15")

    jenkinsServer("org.jenkins-ci.plugins:jackson2-api:2.11.3")
    jenkinsServer("org.jenkins-ci.plugins:credentials:2.3.15")

    kapt("net.java.sezpoz:sezpoz:1.12")

    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
