@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
package ${config.javaPackage()};

import org.junit.jupiter.api.Test;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.test.Deployment;
import org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration test: deploys the skeleton BPMN process and executes it.
 * Passes on first run without any modification.
 */
class ProcessIT {

    @RegisterExtension
    static ProcessEngineExtension extension = ProcessEngineExtension.builder()
            .useDefaultConfiguration()
            .build();

    @Test
    @Deployment(resources = "${config.artifactId()}.bpmn")
    void skeletonProcess_deploys_and_executes_end_to_end() {
        RuntimeService runtimeService = extension.getRuntimeService();

        var processInstance = runtimeService.startProcessInstanceByKey("${config.artifactId()}");

        assertThat(processInstance).isNotNull();
        assertThat(processInstance.isEnded()).isTrue();
    }
}
