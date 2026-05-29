@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
package ${config.javaPackage()};

import com.example.my_process_app.delegate.SkeletonDelegate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.operaton.bpm.engine.impl.mock.MockExpressionManager;
import org.operaton.bpm.engine.impl.mock.Mocks;
import org.operaton.bpm.engine.test.Deployment;
import org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration test: deploys the skeleton BPMN process and executes it.
 * Passes on first run without any modification.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ProcessIT {

    @RegisterExtension
    static ProcessEngineExtension extension = ProcessEngineExtension.builder()
            .useProcessEngine(new StandaloneInMemProcessEngineConfiguration()
                    .setExpressionManager(new MockExpressionManager())
                    .buildProcessEngine())
            .build();

    @BeforeEach
    void setUp() {
        Mocks.register("skeletonDelegate", new SkeletonDelegate());
    }

    @AfterEach
    void tearDown() {
        Mocks.reset();
    }

    @Test
    @Deployment(resources = "${config.artifactId()}.bpmn")
    void skeletonProcess_deploys_and_executes_end_to_end() {
        RuntimeService runtimeService = extension.getRuntimeService();

        var processInstance = runtimeService.startProcessInstanceByKey("${config.artifactId()}");

        assertThat(processInstance).isNotNull();
        assertThat(processInstance.isEnded()).isTrue();
    }
}
