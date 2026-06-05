@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
-- UC-01 Leave Request: seed users and groups for Operaton identity
-- Users: alice (employee), bob (manager), carol (HR)

-- Groups
MERGE INTO ACT_ID_GROUP (ID_, REV_, NAME_, TYPE_) KEY(ID_)
    VALUES ('employees', 1, 'Employees', 'WORKFLOW');
MERGE INTO ACT_ID_GROUP (ID_, REV_, NAME_, TYPE_) KEY(ID_)
    VALUES ('managers', 1, 'Managers', 'WORKFLOW');
MERGE INTO ACT_ID_GROUP (ID_, REV_, NAME_, TYPE_) KEY(ID_)
    VALUES ('hr', 1, 'HR Department', 'WORKFLOW');

-- Users
MERGE INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_) KEY(ID_)
    VALUES ('alice', 1, 'Alice', 'Employee', 'alice@example.com', 'alice');
MERGE INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_) KEY(ID_)
    VALUES ('bob', 1, 'Bob', 'Manager', 'bob@example.com', 'bob');
MERGE INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_) KEY(ID_)
    VALUES ('carol', 1, 'Carol', 'HR', 'carol@example.com', 'carol');

-- Group memberships
MERGE INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_) KEY(USER_ID_, GROUP_ID_)
    VALUES ('alice', 'employees');
MERGE INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_) KEY(USER_ID_, GROUP_ID_)
    VALUES ('bob', 'managers');
MERGE INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_) KEY(USER_ID_, GROUP_ID_)
    VALUES ('carol', 'hr');
