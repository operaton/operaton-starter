package gg.jte.generated.ondemand.common;
import org.operaton.dev.starter.templates.model.ProjectConfig;
import org.operaton.dev.starter.templates.model.BuildSystem;
import org.operaton.dev.starter.templates.model.ProjectType;
public final class JteciymlGenerated {
	public static final String JTE_NAME = "common/ci.yml.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,3,3,4,4,5,5,6,6,20,20,24,24,25,25,27,27,29,29,30,30,31,31,32,32,32,3,3,3,3};
	public static void render(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, ProjectConfig config) {
		var isMaven = config.buildSystem() == BuildSystem.MAVEN;
		jteOutput.writeContent("\n");
		var isGradleKotlin = config.buildSystem() == BuildSystem.GRADLE_KOTLIN;
		jteOutput.writeContent("\n");
		var buildCmd = isMaven ? "mvn verify --batch-mode" : "./gradlew build";
		jteOutput.writeContent("\nname: CI\n\non:\n  push:\n    branches: [\"main\"]\n  pull_request:\n    branches: [\"main\"]\n\njobs:\n  build:\n    runs-on: ubuntu-latest\n    steps:\n      - uses: actions/checkout@v4\n      - name: Set up Java ");
		jteOutput.writeUserContent(config.javaVersion());
		jteOutput.writeContent("\n        uses: actions/setup-java@v4\n        with:\n          distribution: temurin\n          java-version: \"");
		jteOutput.writeUserContent(config.javaVersion());
		jteOutput.writeContent("\"\n");
		if (isMaven) {
			jteOutput.writeContent("\n          cache: maven\n");
		} else {
			jteOutput.writeContent("\n          cache: gradle\n");
		}
		jteOutput.writeContent("\n      - name: Build with ");
		jteOutput.writeUserContent(isMaven ? "Maven" : "Gradle");
		jteOutput.writeContent("\n        run: ");
		jteOutput.writeUserContent(buildCmd);
		jteOutput.writeContent("\n");
	}
	public static void renderMap(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		ProjectConfig config = (ProjectConfig)params.get("config");
		render(jteOutput, jteHtmlInterceptor, config);
	}
}
