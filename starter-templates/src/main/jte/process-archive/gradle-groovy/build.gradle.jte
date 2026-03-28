@import org.operaton.dev.starter.templates.model.ProjectConfig
@import org.operaton.dev.starter.templates.model.DeploymentTarget
@param ProjectConfig config
!{var tomcat = config.deploymentTarget().filter(t -> t == DeploymentTarget.TOMCAT).isPresent();}
plugins {
    id 'java'
@if(tomcat)
    id 'war'
@endif
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

dependencies {
    compileOnly "org.operaton.bpm:operaton-engine:${config.effectiveOperatonVersion()}"
}

tasks.named('test') {
    useJUnitPlatform()
}

@if(tomcat)
tasks.named('jar') {
    enabled = false
}
@endif
