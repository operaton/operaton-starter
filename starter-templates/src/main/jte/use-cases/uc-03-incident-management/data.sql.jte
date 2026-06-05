@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
-- Seed identity data for Incident Management example
-- Users: frank/frank (first-line), grace/grace (second-line)

MERGE INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_, SALT_, LOCK_EXP_TIME_, ATTEMPTS_, PICTURE_ID_)
KEY (ID_)
VALUES ('frank', 1, 'Frank', 'FirstLine', 'frank@example.com', 'frank', null, null, null, null);

MERGE INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_, SALT_, LOCK_EXP_TIME_, ATTEMPTS_, PICTURE_ID_)
KEY (ID_)
VALUES ('grace', 1, 'Grace', 'SecondLine', 'grace@example.com', 'grace', null, null, null, null);

MERGE INTO ACT_ID_GROUP (ID_, REV_, NAME_, TYPE_)
KEY (ID_)
VALUES ('first-line', 1, 'First-Line Support', 'WORKFLOW');

MERGE INTO ACT_ID_GROUP (ID_, REV_, NAME_, TYPE_)
KEY (ID_)
VALUES ('second-line', 1, 'Second-Line Engineering', 'WORKFLOW');

MERGE INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_)
KEY (USER_ID_, GROUP_ID_)
VALUES ('frank', 'first-line');

MERGE INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_)
KEY (USER_ID_, GROUP_ID_)
VALUES ('grace', 'second-line');
