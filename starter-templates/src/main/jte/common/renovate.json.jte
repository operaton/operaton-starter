@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
{
  "$schema": "https://docs.renovatebot.com/renovate-schema.json",
  "extends": ["config:recommended"],
  "packageRules": [
    {
      "matchPackagePatterns": ["^org.operaton"],
      "groupName": "Operaton BPM",
      "automerge": false
    }
  ],
  "schedule": ["before 9am on monday"],
  "labels": ["dependencies"]
}
