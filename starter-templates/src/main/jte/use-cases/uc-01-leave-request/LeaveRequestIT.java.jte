@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
package ${config.javaPackage()};

import org.junit.jupiter.api.Test;
import org.operaton.bpm.engine.ProcessEngine;
import org.operaton.bpm.engine.RepositoryService;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.TaskService;
import org.operaton.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the Leave Request approval workflow.
 *
 * Covers both the approval path (alice → bob approves → carol records)
 * and the rejection path (alice → bob rejects → alice notified).
 *
 * Run with: ./mvnw test
 */
@ActiveProfiles("h2")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class LeaveRequestIT {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Test
    void processDefinitionIsDeployed() {
        long count = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("leave-request")
                .count();
        assertThat(count).as("leave-request process definition must be deployed").isEqualTo(1);
    }

    @Test
    void approvalPath_aliceStartsBobApproves_carolRecords() {
        // Alice starts a leave request
        var instance = runtimeService.startProcessInstanceByKey("leave-request",
                Map.of("requester", "alice"));

        // Bob (manager) sees and claims the task
        Task managerTask = taskService.createTaskQuery()
                .processInstanceId(instance.getId())
                .taskCandidateGroup("managers")
                .singleResult();
        assertThat(managerTask).as("manager review task must exist").isNotNull();
        assertThat(managerTask.getName()).isEqualTo("Manager Reviews Request");

        taskService.claim(managerTask.getId(), "bob");
        taskService.complete(managerTask.getId(), Map.of("approved", true));

        // Carol (HR) sees the approved leave task
        Task hrTask = taskService.createTaskQuery()
                .processInstanceId(instance.getId())
                .taskCandidateGroup("hr")
                .singleResult();
        assertThat(hrTask).as("HR recording task must exist after approval").isNotNull();
        assertThat(hrTask.getName()).isEqualTo("HR Records Approved Leave");

        taskService.claim(hrTask.getId(), "carol");
        taskService.complete(hrTask.getId());

        // Process must have ended — no active instances
        long activeCount = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instance.getId())
                .count();
        assertThat(activeCount).as("no active instances after approval path completes").isZero();
    }

    @Test
    void rejectionPath_bobRejects_aliceNotified() {
        var instance = runtimeService.startProcessInstanceByKey("leave-request",
                Map.of("requester", "alice"));

        Task managerTask = taskService.createTaskQuery()
                .processInstanceId(instance.getId())
                .taskCandidateGroup("managers")
                .singleResult();
        assertThat(managerTask).isNotNull();

        taskService.claim(managerTask.getId(), "bob");
        taskService.complete(managerTask.getId(), Map.of("approved", false));

        // Alice (employee) sees the rejection notification task
        Task employeeTask = taskService.createTaskQuery()
                .processInstanceId(instance.getId())
                .taskCandidateGroup("employees")
                .singleResult();
        assertThat(employeeTask).as("employee notification task must exist after rejection").isNotNull();
        assertThat(employeeTask.getName()).isEqualTo("Employee Notified of Rejection");

        taskService.claim(employeeTask.getId(), "alice");
        taskService.complete(employeeTask.getId());

        long activeCount = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instance.getId())
                .count();
        assertThat(activeCount).as("no active instances after rejection path completes").isZero();
    }
}
