@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
package ${config.javaPackage()}.delegate;

import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * Skeleton Java delegate wired to the service task in ${config.artifactId()}.bpmn.
 *
 * <p>Replace this implementation with your actual business logic.
 */
@Component("skeletonDelegate")
public class SkeletonDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        // TODO: implement your business logic here
        System.out.println("Executing process: " + execution.getProcessDefinitionId());
    }
}
