@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
-- Seed identity data for Loan Application example
-- User: eve / eve (underwriters group)

MERGE INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_, SALT_, LOCK_EXP_TIME_, ATTEMPTS_, PICTURE_ID_)
KEY (ID_)
VALUES ('eve', 1, 'Eve', 'Underwriter', 'eve@example.com', 'eve', null, null, null, null);

MERGE INTO ACT_ID_GROUP (ID_, REV_, NAME_, TYPE_)
KEY (ID_)
VALUES ('underwriters', 1, 'Underwriters', 'WORKFLOW');

MERGE INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_)
KEY (USER_ID_, GROUP_ID_)
VALUES ('eve', 'underwriters');
