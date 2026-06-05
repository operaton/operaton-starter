@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
package ${config.javaPackage()}.delegate;

import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(NotificationDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String activityId = execution.getCurrentActivityId();
        Object riskLevel = execution.getVariable("riskLevel");
        Object creditScore = execution.getVariable("creditScore");

        if (activityId != null && activityId.contains("Reject")) {
            log.info("Loan application REJECTED. Risk level: {}, Credit score: {}", riskLevel, creditScore);
        } else {
            log.info("Loan application APPROVED. Risk level: {}, Credit score: {}", riskLevel, creditScore);
        }
    }
}
