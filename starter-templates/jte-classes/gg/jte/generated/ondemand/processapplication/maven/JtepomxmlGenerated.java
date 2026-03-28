package gg.jte.generated.ondemand.processapplication.maven;
import org.operaton.dev.starter.templates.model.ProjectConfig;
import org.operaton.dev.starter.templates.model.VersionConstants;
public final class JtepomxmlGenerated {
	public static final String JTE_NAME = "process-application/maven/pom.xml.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,2,2,12,12,12,16,16,17,17,19,19,22,22,23,23,24,24,32,32,39,39,43,43,50,50,53,53,91,91,91,2,2,2,2};
	public static void render(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, ProjectConfig config) {
		jteOutput.writeContent("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n  <modelVersion>4.0.0</modelVersion>\n\n  <parent>\n    <groupId>org.springframework.boot</groupId>\n    <artifactId>spring-boot-starter-parent</artifactId>\n    <version>");
		jteOutput.writeUserContent(VersionConstants.SPRING_BOOT_VERSION);
		jteOutput.writeContent("</version>\n    <relativePath/>\n  </parent>\n\n  <groupId>");
		jteOutput.writeUserContent(config.groupId());
		jteOutput.writeContent("</groupId>\n  <artifactId>");
		jteOutput.writeUserContent(config.artifactId());
		jteOutput.writeContent("</artifactId>\n  <version>0.0.1-SNAPSHOT</version>\n  <name>");
		jteOutput.writeUserContent(config.projectName());
		jteOutput.writeContent("</name>\n\n  <properties>\n    <java.version>");
		jteOutput.writeUserContent(config.javaVersion());
		jteOutput.writeContent("</java.version>\n    <maven.compiler.release>");
		jteOutput.writeUserContent(config.javaVersion());
		jteOutput.writeContent("</maven.compiler.release>\n    <operaton.version>");
		jteOutput.writeUserContent(config.effectiveOperatonVersion());
		jteOutput.writeContent("</operaton.version>\n  </properties>\n\n  <dependencyManagement>\n    <dependencies>\n      <dependency>\n        <groupId>org.operaton.bpm</groupId>\n        <artifactId>operaton-bom</artifactId>\n        <version>");
		jteOutput.writeUserContent(config.effectiveOperatonVersion());
		jteOutput.writeContent("</version>\n        <type>pom</type>\n        <scope>import</scope>\n      </dependency>\n    </dependencies>\n  </dependencyManagement>\n\n");
		if (config.hasCustomMavenRegistry()) {
			jteOutput.writeContent("\n  <repositories>\n    <repository>\n      <id>starter-default-repository</id>\n      <url>");
			jteOutput.writeUserContent(config.mavenRegistryUrl());
			jteOutput.writeContent("</url>\n    </repository>\n  </repositories>\n\n  <pluginRepositories>\n    <pluginRepository>\n      <id>starter-default-plugin-repository</id>\n      <url>");
			jteOutput.writeUserContent(config.mavenRegistryUrl());
			jteOutput.writeContent("</url>\n    </pluginRepository>\n  </pluginRepositories>\n");
		}
		jteOutput.writeContent("\n\n  <dependencies>\n    <dependency>\n      <groupId>org.operaton.bpm.springboot</groupId>\n      <artifactId>operaton-bpm-spring-boot-starter</artifactId>\n    </dependency>\n    <dependency>\n      <groupId>org.springframework.boot</groupId>\n      <artifactId>spring-boot-starter-web</artifactId>\n    </dependency>\n    <dependency>\n      <groupId>com.h2database</groupId>\n      <artifactId>h2</artifactId>\n      <scope>runtime</scope>\n    </dependency>\n    <dependency>\n      <groupId>org.springframework.boot</groupId>\n      <artifactId>spring-boot-starter-test</artifactId>\n      <scope>test</scope>\n    </dependency>\n    <dependency>\n      <groupId>org.operaton.bpm</groupId>\n      <artifactId>operaton-bpm-junit5</artifactId>\n      <scope>test</scope>\n    </dependency>\n  </dependencies>\n\n  <build>\n    <plugins>\n      <plugin>\n        <groupId>org.springframework.boot</groupId>\n        <artifactId>spring-boot-maven-plugin</artifactId>\n      </plugin>\n    </plugins>\n  </build>\n\n</project>\n");
	}
	public static void renderMap(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		ProjectConfig config = (ProjectConfig)params.get("config");
		render(jteOutput, jteHtmlInterceptor, config);
	}
}
