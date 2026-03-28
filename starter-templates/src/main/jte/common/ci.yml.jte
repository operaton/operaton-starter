@import org.operaton.dev.starter.templates.model.ProjectConfig
@import org.operaton.dev.starter.templates.model.BuildSystem
@import org.operaton.dev.starter.templates.model.ProjectType
@param ProjectConfig config
!{var isMaven = config.buildSystem() == BuildSystem.MAVEN;}
!{var isGradleKotlin = config.buildSystem() == BuildSystem.GRADLE_KOTLIN;}
!{var buildCmd = isMaven ? "mvn verify --batch-mode" : "./gradlew build";}
name: CI

on:
  push:
    branches: ["main"]
  pull_request:
    branches: ["main"]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up Java ${config.javaVersion()}
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "${config.javaVersion()}"
@if(isMaven)
          cache: maven
@else
          cache: gradle
@endif
      - name: Build with ${isMaven ? "Maven" : "Gradle"}
        run: ${buildCmd}
