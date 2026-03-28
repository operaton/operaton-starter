@import org.operaton.dev.starter.templates.model.ProjectConfig
@import org.operaton.dev.starter.templates.model.VersionConstants
@param ProjectConfig config
plugins {
    id 'java'
    id 'org.springframework.boot' version '${VersionConstants.SPRING_BOOT_VERSION}'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = '${config.groupId()}'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(${config.javaVersion()})
    }
}

repositories {
@if(config.hasCustomMavenRegistry())
    maven { url = uri('${config.mavenRegistryUrl()}') }
@else
    mavenCentral()
@endif
}

dependencyManagement {
    imports {
        mavenBom "org.operaton.bpm:operaton-bom:${config.effectiveOperatonVersion()}"
    }
}

dependencies {
    implementation 'org.operaton.bpm.springboot:operaton-bpm-spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    runtimeOnly 'com.h2database:h2'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.operaton.bpm:operaton-bpm-junit5'
}

tasks.named('test') {
    useJUnitPlatform()
}
