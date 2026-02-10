plugins {
  java
  application
  id("io.ebean") version "17.2.1"
  id("co.uzzu.dotenv.gradle") version "4.0.0"
}

group = "dev.kuehni"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  maven("https://jitpack.io")
}

dependencies {
  // Environment
  implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
  // Semantic Annotations
  implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
  // OpenAI API Client
  implementation("com.openai:openai-java:4.15.0")
  // Java Parsing
  implementation("com.github.javaparser:javaparser-core:3.28.0")
  // CSV
  implementation("com.opencsv:opencsv:5.12.0")
  // Logging
  implementation("org.slf4j:slf4j-api:2.0.17")
  implementation("ch.qos.logback:logback-classic:1.5.26")
  // Sortable Unique IDs
  implementation("com.github.f4b6a3:uuid-creator:6.1.1")
  // ORM + Database Driver
  implementation("io.ebean:ebean:17.2.1")
  implementation("io.ebean:ebean-ddl-generator:17.2.1")
  implementation("com.h2database:h2:2.4.240")
  annotationProcessor("io.ebean:querybean-generator:17.2.1")

  // Testing Framework
  testImplementation(platform("org.junit:junit-bom:5.10.0"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  // Testing Platform
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
  useJUnitPlatform()
}

tasks.withType<JavaExec>().configureEach {
  jvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

application {
  mainClass = "dev.kuehni.llmtestgen.app.Main"
}

tasks.named<CreateStartScripts>("startScripts") {
  doLast {
    windowsScript.writeText(
      windowsScript.readText()
        .replace("^set CLASSPATH=.*$".toRegex(RegexOption.MULTILINE), "set CLASSPATH=%APP_HOME%\\\\lib\\\\*")
    )
  }
}

tasks.jar {
  manifest {
    attributes["Main-Class"] = application.mainClass
    attributes["Enable-Native-Access"] = "ALL-UNNAMED"
  }
}
