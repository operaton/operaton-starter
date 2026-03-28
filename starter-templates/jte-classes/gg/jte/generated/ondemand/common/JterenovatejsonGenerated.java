package gg.jte.generated.ondemand.common;
import org.operaton.dev.starter.templates.model.ProjectConfig;
public final class JterenovatejsonGenerated {
	public static final String JTE_NAME = "common/renovate.json.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,1,1,15,15,15,15,1,1,1,1};
	public static void render(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, ProjectConfig config) {
		jteOutput.writeContent("{\n  \"$schema\": \"https://docs.renovatebot.com/renovate-schema.json\",\n  \"extends\": [\"config:recommended\"],\n  \"packageRules\": [\n    {\n      \"matchPackagePatterns\": [\"^org.operaton\"],\n      \"groupName\": \"Operaton BPM\",\n      \"automerge\": false\n    }\n  ],\n  \"schedule\": [\"before 9am on monday\"],\n  \"labels\": [\"dependencies\"]\n}\n");
	}
	public static void renderMap(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		ProjectConfig config = (ProjectConfig)params.get("config");
		render(jteOutput, jteHtmlInterceptor, config);
	}
}
