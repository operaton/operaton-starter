package gg.jte.generated.ondemand.processapplication;
import org.operaton.dev.starter.templates.model.ProjectConfig;
public final class JteprocessbpmnGenerated {
	public static final String JTE_NAME = "process-application/process.bpmn.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,1,1,10,10,10,10,10,18,18,28,28,28,1,1,1,1};
	public static void render(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, ProjectConfig config) {
		jteOutput.writeContent("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n             xmlns:operaton=\"http://operaton.org/schema/1.0/bpmn\"\n             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n             targetNamespace=\"http://bpmn.io/schema/bpmn\"\n             exporter=\"Operaton Modeler\"\n             exporterVersion=\"1.0.0\">\n\n  <process id=\"");
		jteOutput.writeUserContent(config.artifactId());
		jteOutput.writeContent("\" name=\"");
		jteOutput.writeUserContent(config.projectName());
		jteOutput.writeContent("\" isExecutable=\"true\">\n\n    <startEvent id=\"StartEvent_1\" name=\"Start\"/>\n\n    <sequenceFlow id=\"Flow_start_to_task\" sourceRef=\"StartEvent_1\" targetRef=\"Task_skeleton\"/>\n\n    <serviceTask id=\"Task_skeleton\"\n                 name=\"Skeleton Task\"\n                 operaton:delegateExpression=\"");
		jteOutput.writeUserContent('$');
		jteOutput.writeContent("{skeletonDelegate}\">\n    </serviceTask>\n\n    <sequenceFlow id=\"Flow_task_to_end\" sourceRef=\"Task_skeleton\" targetRef=\"EndEvent_1\"/>\n\n    <endEvent id=\"EndEvent_1\" name=\"End\"/>\n\n  </process>\n\n</definitions>\n");
	}
	public static void renderMap(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		ProjectConfig config = (ProjectConfig)params.get("config");
		render(jteOutput, jteHtmlInterceptor, config);
	}
}
