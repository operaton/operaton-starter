@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
package ${config.javaPackage()}.delegate;

import ${config.javaPackage()}.VacationBalanceService;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class FinalizeLeaveApprovalDelegate implements JavaDelegate {

    private final VacationBalanceService vacationBalanceService;

    public FinalizeLeaveApprovalDelegate(VacationBalanceService vacationBalanceService) {
        this.vacationBalanceService = vacationBalanceService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String requester = (String) execution.getVariable("requester");
        if (requester == null || requester.isBlank()) {
            throw new IllegalStateException("Leave requester must be present before finalizing approved leave.");
        }
        int durationDays = readDurationDays(execution.getVariable("durationDays"));

        int remainingVacationDays = vacationBalanceService.deductVacationDays(requester, durationDays);
        execution.setVariable("remainingVacationDays", remainingVacationDays);
    }

    private int readDurationDays(Object durationValue) {
        int durationDays = durationValue instanceof Number number
                ? number.intValue()
                : Integer.parseInt(String.valueOf(durationValue));
        if (durationDays < 1) {
            throw new IllegalArgumentException("durationDays must be at least 1 before finalizing approved leave.");
        }
        return durationDays;
    }
}
