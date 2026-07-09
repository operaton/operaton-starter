@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
package ${config.javaPackage()};

import java.util.logging.Logger;

import org.operaton.bpm.engine.authorization.Groups;
import org.operaton.bpm.engine.IdentityService;
import org.operaton.bpm.engine.identity.Group;
import org.operaton.bpm.engine.identity.User;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {
    private static final Logger LOGGER = Logger.getLogger(DataInitializer.class.getName());

    private final IdentityService identityService;
    private final VacationBalanceService vacationBalanceService;

    public DataInitializer(IdentityService identityService, VacationBalanceService vacationBalanceService) {
        this.identityService = identityService;
        this.vacationBalanceService = vacationBalanceService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if(identityService.isReadOnly()) {
            LOGGER.info("Identity service provider is read-only, not creating any demo users.");
            return;
        }

        createGroupIfAbsent(Groups.OPERATON_ADMIN, "Operaton Administrators", Groups.GROUP_TYPE_SYSTEM);
        createGroupIfAbsent("employees", "Employees", "WORKFLOW");
        createGroupIfAbsent("managers", "Managers", "WORKFLOW");
        createGroupIfAbsent("hr", "HR Department", "WORKFLOW");

        createUserIfAbsent("admin", "Admin", "User", "admin@example.com", "admin");
        createUserIfAbsent("alice", "Alice", "Employee", "alice@example.com", "alice");
        createUserIfAbsent("bob", "Bob", "Manager", "bob@example.com", "bob");
        createUserIfAbsent("carol", "Carol", "HR", "carol@example.com", "carol");

        createMembershipIfAbsent("admin", "operaton-admin");
        createMembershipIfAbsent("alice", "employees");
        createMembershipIfAbsent("bob", "managers");
        createMembershipIfAbsent("carol", "hr");

        // create ADMIN authorizations on all built-in resources
        for (Resource resource : Resources.values()) {
            if(authorizationService.createAuthorizationQuery().groupIdIn(Groups.OPERATON_ADMIN).resourceType(resource).resourceId(ANY).count() == 0) {
                AuthorizationEntity userAdminAuth = new AuthorizationEntity(AUTH_TYPE_GRANT);
                userAdminAuth.setGroupId(Groups.OPERATON_ADMIN);
                userAdminAuth.setResource(resource);
                userAdminAuth.setResourceId(ANY);
                userAdminAuth.addPermission(ALL);
                authorizationService.saveAuthorization(userAdminAuth);
            }
        }


        vacationBalanceService.seedBalanceIfAbsent("alice", 30);
    }

    private void createGroupIfAbsent(String id, String name, String type) {
        if (identityService.createGroupQuery().groupId(id).count() == 0) {
            Group group = identityService.newGroup(id);
            group.setName(name);
            group.setType(type);
            identityService.saveGroup(group);
        }
    }

    private void createUserIfAbsent(String id, String first, String last, String email, String password) {
        if (identityService.createUserQuery().userId(id).count() == 0) {
            User user = identityService.newUser(id);
            user.setFirstName(first);
            user.setLastName(last);
            user.setEmail(email);
            user.setPassword(password);
            identityService.saveUser(user);
        }
    }

    private void createMembershipIfAbsent(String userId, String groupId) {
        if (identityService.createUserQuery().userId(userId).memberOfGroup(groupId).count() == 0) {
            identityService.createMembership(userId, groupId);
        }
    }
}
