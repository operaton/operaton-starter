@import org.operaton.dev.starter.templates.model.ProjectConfig
@import org.operaton.dev.starter.templates.model.BuildSystem
@param ProjectConfig config
!{var packageEcosystem = config.buildSystem() == BuildSystem.MAVEN ? "maven" : "gradle";}
version: 2
updates:
  - package-ecosystem: "${packageEcosystem}"
    directory: "/"
    schedule:
      interval: "weekly"
    labels:
      - "dependencies"
