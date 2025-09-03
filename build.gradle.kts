import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.gradle.kotlin.dsl.named

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.noarg") version "2.2.10"
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.asciidoctor.jvm.convert") version "3.3.2"
}

val asciidoctorExt by configurations.creating

configurations {
	asciidoctorExt
}

noArg {
	annotation("com.bentley.core.NoArg")
}

group = "com.bentley"
version = "0.0.1-SNAPSHOT"
description = "Bentley Auth"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

extra["springModulithVersion"] = "1.4.1"

dependencyManagement {
	imports {
		mavenBom("org.springframework.modulith:spring-modulith-bom:${property("springModulithVersion")}")
		mavenBom("org.springframework.restdocs:spring-restdocs-bom:3.0.5")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-quartz")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.5")
	implementation("org.springframework.modulith:spring-modulith-starter-core")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2+")
	implementation("com.auth0:java-jwt:4.5.0")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.mysql:mysql-connector-j")
	asciidoctorExt("org.springframework.restdocs:spring-restdocs-asciidoctor")
	testRuntimeOnly("com.h2database:h2")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.5")
	testImplementation("org.springframework.modulith:spring-modulith-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

val snippetsDir by extra { file("build/generated-snippets") }

tasks.test {
	outputs.dir(snippetsDir)
}

tasks.named<AsciidoctorTask>("asciidoctor") {
	inputs.dir(snippetsDir)
	configurations(asciidoctorExt.name)
	dependsOn(tasks.test)
}

tasks.bootJar {
	dependsOn(tasks.asciidoctor)
	from ("${tasks.asciidoctor.get().outputDir}") {
		into("static/docs")
	}
}
