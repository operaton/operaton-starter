package gg.jte.generated.ondemand.processapplication;
import org.operaton.dev.starter.templates.model.ProjectConfig;
public final class JteProcessITjavaGenerated {
	public static final String JTE_NAME = "process-application/ProcessIT.java.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,1,1,2,2,2,24,24,28,28,34,34,34,1,1,1,1};
	public static void render(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, ProjectConfig config) {
		jteOutput.writeContent("package ");
		jteOutput.writeUserContent(config.javaPackage());
		jteOutput.writeContent(";\n\nimport org.junit.jupiter.api.Test;\nimport org.operaton.bpm.engine.RuntimeService;\nimport org.operaton.bpm.engine.test.Deployment;\nimport org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;\nimport org.junit.jupiter.api.extension.RegisterExtension;\n\nimport static org.assertj.core.api.Assertions.assertThat;\n\n/**\n * End-to-end integration test: deploys the skeleton BPMN process and executes it.\n * Passes on first run without any modification.\n */\nclass ProcessIT {\n\n    @RegisterExtension\n    static ProcessEngineExtension extension = ProcessEngineExtension.builder()\n            .useDefaultConfiguration()\n            .build();\n\n    @Test\n    @Deployment(resources = \"");
		jteOutput.writeUserContent(config.artifactId());
		jteOutput.writeContent(".bpmn\")\n    void skeletonProcess_deploys_and_executes_end_to_end() {\n        RuntimeService runtimeService = extension.getRuntimeService();\n\n        var processInstance = runtimeService.startProcessInstanceByKey(\"");
		jteOutput.writeUserContent(config.artifactId());
		jteOutput.writeContent("\");\n\n        assertThat(processInstance).isNotNull();\n        assertThat(processInstance.isEnded()).isTrue();\n    }\n}\n");
	}
	public static void renderMap(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		ProjectConfig config = (ProjectConfig)params.get("config");
		render(jteOutput, jteHtmlInterceptor, config);
	}
}
