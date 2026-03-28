package gg.jte.generated.ondemand.processapplication.delegate;
import org.operaton.dev.starter.templates.model.ProjectConfig;
public final class JteSkeletonDelegatejavaGenerated {
	public static final String JTE_NAME = "process-application/delegate/SkeletonDelegate.java.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,1,1,2,2,2,9,9,22,22,22,1,1,1,1};
	public static void render(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, ProjectConfig config) {
		jteOutput.writeContent("package ");
		jteOutput.writeUserContent(config.javaPackage());
		jteOutput.writeContent(".delegate;\n\nimport org.operaton.bpm.engine.delegate.DelegateExecution;\nimport org.operaton.bpm.engine.delegate.JavaDelegate;\nimport org.springframework.stereotype.Component;\n\n/**\n * Skeleton Java delegate wired to the service task in ");
		jteOutput.writeUserContent(config.artifactId());
		jteOutput.writeContent(".bpmn.\n *\n * <p>Replace this implementation with your actual business logic.\n */\n@Component(\"skeletonDelegate\")\npublic class SkeletonDelegate implements JavaDelegate {\n\n    @Override\n    public void execute(DelegateExecution execution) {\n        // TODO: implement your business logic here\n        System.out.println(\"Executing process: \" + execution.getProcessDefinitionId());\n    }\n}\n");
	}
	public static void renderMap(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		ProjectConfig config = (ProjectConfig)params.get("config");
		render(jteOutput, jteHtmlInterceptor, config);
	}
}
