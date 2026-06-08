@import org.operaton.dev.starter.templates.model.ProjectConfig
@import org.operaton.dev.starter.templates.model.VersionConstants
@param ProjectConfig config
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>${config.groupId()}</groupId>
  <artifactId>${config.artifactId()}</artifactId>
  <version>${VersionConstants.INITIAL_PROJECT_VERSION}</version>
  <name>${config.projectName()}</name>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <java.version>${config.javaVersion()}</java.version>
    <maven.compiler.release>${config.javaVersion()}</maven.compiler.release>
    <spring-boot.version>${VersionConstants.SPRING_BOOT_VERSION}</spring-boot.version>
    <operaton.version>${config.effectiveOperatonVersion()}</operaton.version>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${'$'}{spring-boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
      <dependency>
        <groupId>org.operaton.bpm</groupId>
        <artifactId>operaton-bom</artifactId>
        <version>${'$'}{operaton.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

@if(config.hasCustomMavenRegistry())
  <repositories>
    <repository>
      <id>starter-default-repository</id>
      <url>${config.mavenRegistryUrl()}</url>
    </repository>
  </repositories>

  <pluginRepositories>
    <pluginRepository>
      <id>starter-default-plugin-repository</id>
      <url>${config.mavenRegistryUrl()}</url>
    </pluginRepository>
  </pluginRepositories>
@endif

  <dependencies>
    <!-- Operaton Spring Boot with embedded engine + Tasklist/Cockpit/Admin UI -->
    <dependency>
      <groupId>org.operaton.bpm.springboot</groupId>
      <artifactId>operaton-bpm-spring-boot-starter-webapp</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
    <!-- H2 in-memory database — no external database needed -->
    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <scope>runtime</scope>
    </dependency>
    <!-- PostgreSQL driver — used by default; H2 used when h2 profile active -->
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
    </dependency>
    <!-- Spring Mail — used by LeaveRejectionEmailDelegate; Mailpit for local testing -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.operaton.bpm</groupId>
      <artifactId>operaton-bpm-junit5</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <version>${'$'}{spring-boot.version}</version>
        <configuration>
          <layout>ZIP</layout>
        </configuration>
        <executions>
          <execution>
            <goals>
              <goal>repackage</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>

</project>
