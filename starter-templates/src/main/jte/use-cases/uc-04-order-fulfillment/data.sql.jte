@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
-- Seed identity data for Order Fulfillment example
-- User: dave/dave (warehouse group)

MERGE INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_, SALT_, LOCK_EXP_TIME_, ATTEMPTS_, PICTURE_ID_)
KEY (ID_)
VALUES ('dave', 1, 'Dave', 'Warehouse', 'dave@example.com', 'dave', null, null, null, null);

MERGE INTO ACT_ID_GROUP (ID_, REV_, NAME_, TYPE_)
KEY (ID_)
VALUES ('warehouse', 1, 'Warehouse Team', 'WORKFLOW');

MERGE INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_)
KEY (USER_ID_, GROUP_ID_)
VALUES ('dave', 'warehouse');

-- Operaton admin user (created on first boot if not existent)
MERGE INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_, SALT_, LOCK_EXP_TIME_, ATTEMPTS_, PICTURE_ID_)
KEY (ID_)
VALUES ('admin', 1, 'Admin', 'User', 'admin@example.com', 'admin', null, null, null, null);

MERGE INTO ACT_ID_GROUP (ID_, REV_, NAME_, TYPE_) KEY(ID_)
    VALUES ('operaton-admin', 1, 'Operaton Administrators', 'SYSTEM');

MERGE INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_) KEY(USER_ID_, GROUP_ID_)
    VALUES ('admin', 'operaton-admin');
