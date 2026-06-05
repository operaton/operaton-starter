@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
package ${config.javaPackage()};

import org.junit.jupiter.api.*;
import org.operaton.bpm.engine.ProcessEngine;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.TaskService;
import org.operaton.bpm.engine.runtime.ProcessInstance;
import org.operaton.bpm.engine.task.Task;
import org.operaton.bpm.engine.variable.Variables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("h2")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ContextConfiguration(initializers = OrderFulfillmentIT.WireMockInitializer.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderFulfillmentIT {

    @Container
    static WireMockContainer wireMock = new WireMockContainer("wiremock/wiremock:3.5.4")
            .withMappingFromResource("inventory-in-stock.json")
            .withMappingFromResource("inventory-out-of-stock.json")
            .withMappingFromResource("payment-success.json")
            .withMappingFromResource("notify-customer.json")
            .withMappingFromResource("notify-backorder.json")
            .waitingFor(Wait.forHttp("/__admin/mappings"));

    static class WireMockInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext ctx) {
            String baseUrl = "http://" + wireMock.getHost() + ":" + wireMock.getMappedPort(8080);
            TestPropertyValues.of(
                    "inventory.service.url=" + baseUrl,
                    "payment.service.url=" + baseUrl,
                    "notification.service.url=" + baseUrl
            ).applyTo(ctx.getEnvironment());
        }
    }

    @Autowired
    private ProcessEngine processEngine;

    @Test
    @Order(1)
    void processDefinitionIsDeployed() {
        long count = processEngine.getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionKey("order-fulfillment")
                .count();
        assertEquals(1, count, "order-fulfillment process must be deployed");
    }

    @Test
    @Order(2)
    void inStockPath_createsWarehouseTask() {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        TaskService taskService = processEngine.getTaskService();

        // in-stock orderId (matches /inventory/.* → available: true)
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                "order-fulfillment",
                Variables.putValue("orderId", "ORD-001"));

        // Warehouse pack & ship task should be created
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(instance.getId())
                .taskCandidateGroup("warehouse")
                .list();
        assertFalse(tasks.isEmpty(), "Expected warehouse pack & ship task for in-stock order");

        // Dave completes the warehouse task
        taskService.claim(tasks.get(0).getId(), "dave");
        taskService.complete(tasks.get(0).getId());

        // Process should be complete
        long activeCount = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instance.getId())
                .count();
        assertEquals(0, activeCount, "No active instances should remain after in-stock path");
    }

    @Test
    @Order(3)
    void outOfStockPath_endsDirectly() {
        RuntimeService runtimeService = processEngine.getRuntimeService();

        // out-of-stock orderId (matches /inventory/out-of-stock-.* → available: false)
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                "order-fulfillment",
                Variables.putValue("orderId", "out-of-stock-ORD-002"));

        // Process should end directly without any warehouse task
        long activeCount = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instance.getId())
                .count();
        assertEquals(0, activeCount, "No active instances should remain after out-of-stock path");
    }
}
