@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
package ${config.javaPackage()};

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.operaton.bpm.engine.DecisionService;
import org.operaton.bpm.engine.test.Deployment;
import org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;
import org.operaton.bpm.engine.variable.Variables;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test: deploys the skeleton DMN decision table and evaluates it.
 * Passes on first run without any modification.
 */
class DecisionIT {

    @RegisterExtension
    static ProcessEngineExtension extension = ProcessEngineExtension.builder()
            .useDefaultConfiguration()
            .build();

    @Test
    @Deployment(resources = "skeleton-decision.dmn")
    void skeletonDecision_accepted_for_standard_category() {
        DecisionService decisionService = extension.getDecisionService();

        var result = decisionService.evaluateDecisionByKey("skeleton-decision")
                .variables(Variables.createVariables().putValue("category", "standard"))
                .evaluate();

        assertThat(result.getSingleResult().<String>getSingleEntry()).isEqualTo("ACCEPTED");
    }

    @Test
    @Deployment(resources = "skeleton-decision.dmn")
    void skeletonDecision_rejected_for_unknown_category() {
        DecisionService decisionService = extension.getDecisionService();

        var result = decisionService.evaluateDecisionByKey("skeleton-decision")
                .variables(Variables.createVariables().putValue("category", "unknown"))
                .evaluate();

        assertThat(result.getSingleResult().<String>getSingleEntry()).isEqualTo("REJECTED");
    }
}
