package gg.jte.generated.ondemand.processapplication;
import org.operaton.dev.starter.templates.model.ProjectConfig;
public final class JteapplicationpropertiesGenerated {
	public static final String JTE_NAME = "process-application/application.properties.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,1,1,2,2,2,13,13,13,1,1,1,1};
	public static void render(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, ProjectConfig config) {
		jteOutput.writeContent("spring.application.name=");
		jteOutput.writeUserContent(config.projectName());
		jteOutput.writeContent("\nserver.port=8080\n\n# H2 in-memory datasource — replace with a persistent DB for production\nspring.datasource.url=jdbc:h2:mem:operaton;DB_CLOSE_DELAY=-1\nspring.datasource.driver-class-name=org.h2.Driver\nspring.datasource.username=sa\nspring.datasource.password=\n\n# Operaton configuration\noperaton.bpm.auto-deployment-enabled=true\n");
	}
	public static void renderMap(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		ProjectConfig config = (ProjectConfig)params.get("config");
		render(jteOutput, jteHtmlInterceptor, config);
	}
}
