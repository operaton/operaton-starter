@import org.operaton.dev.starter.templates.model.ProjectConfig
@import org.operaton.dev.starter.templates.model.DeploymentTarget
@import org.operaton.dev.starter.templates.model.VersionConstants
@param ProjectConfig config
!{var packaging = config.deploymentTarget().filter(t -> t == DeploymentTarget.TOMCAT).isPresent() ? "war" : "jar";}
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>${config.groupId()}</groupId>
  <artifactId>${config.artifactId()}</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <packaging>${packaging}</packaging>
  <name>${config.projectName()}</name>

  <properties>
    <java.version>${config.javaVersion()}</java.version>
    <maven.compiler.release>${config.javaVersion()}</maven.compiler.release>
    <operaton.version>${VersionConstants.OPERATON_VERSION}</operaton.version>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.operaton.bpm</groupId>
        <artifactId>operaton-bom</artifactId>
        <version>${VersionConstants.OPERATON_VERSION}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <dependency>
      <groupId>org.operaton.bpm</groupId>
      <artifactId>operaton-engine</artifactId>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
          <includes>
            <include>**/*Test.java</include>
          </includes>
        </configuration>
      </plugin>
    </plugins>
  </build>

</project>
