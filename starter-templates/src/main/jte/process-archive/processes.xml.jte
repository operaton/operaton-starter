@import org.operaton.dev.starter.templates.model.ProjectConfig
@import org.operaton.dev.starter.templates.model.DeploymentTarget
@param ProjectConfig config
!{var target = config.deploymentTarget().orElse(DeploymentTarget.STANDALONE_ENGINE);}
!{var engineRef = target == DeploymentTarget.TOMCAT ? "default :ProcessEngine" : "default";}
<?xml version="1.0" encoding="UTF-8"?>
<process-application
    xmlns="http://www.operaton.org/schema/1.0/ProcessApplication"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

  <process-archive name="${config.artifactId()}">
    <process-engine>${engineRef}</process-engine>
    <properties>
      <property name="isDeleteUponUndeploy">false</property>
      <property name="isScanForProcessDefinitions">true</property>
    </properties>
  </process-archive>

</process-application>
