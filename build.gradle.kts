import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.0.1"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.7.22"
	kotlin("plugin.spring") version "1.7.22"
}

group = "pl.umk.mat.zesp01.pz2022"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-mail:3.0.5")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.mindrot:jbcrypt:0.4")
	implementation("com.auth0:java-jwt:4.2.1")
	implementation("com.google.code.gson:gson:2.10.1")
	implementation("org.springframework:spring-context-support:6.0.7")
	implementation("org.apache.commons:commons-lang3:3.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation")
	testImplementation("org.testcontainers:mongodb:1.17.6")
	testImplementation("org.testcontainers:junit-jupiter:1.17.6")
	testImplementation("org.assertj:assertj-core:3.24.2")
	testImplementation("com.icegreen:greenmail-junit5:2.0.0")
	testImplementation("org.testcontainers:junit-jupiter:1.17.6")
	testImplementation("jakarta.validation:jakarta.validation-api:2.0.2")
	testImplementation("org.awaitility:awaitility:4.2.0")

}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
