package gg.jte.generated.ondemand.processapplication;
import org.operaton.dev.starter.templates.model.ProjectConfig;
public final class JteApplicationjavaGenerated {
	public static final String JTE_NAME = "process-application/Application.java.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,1,1,2,2,2,14,14,14,1,1,1,1};
	public static void render(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, ProjectConfig config) {
		jteOutput.writeContent("package ");
		jteOutput.writeUserContent(config.javaPackage());
		jteOutput.writeContent(";\n\nimport org.springframework.boot.SpringApplication;\nimport org.springframework.boot.autoconfigure.SpringBootApplication;\n\n@SpringBootApplication\npublic class Application {\n\n    public static void main(String[] args) {\n        SpringApplication.run(Application.class, args);\n    }\n}\n");
	}
	public static void renderMap(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		ProjectConfig config = (ProjectConfig)params.get("config");
		render(jteOutput, jteHtmlInterceptor, config);
	}
}
