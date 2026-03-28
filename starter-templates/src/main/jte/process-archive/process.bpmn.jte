@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             targetNamespace="http://bpmn.io/schema/bpmn"
             exporter="Operaton Modeler"
             exporterVersion="1.0.0">

  <process id="${config.artifactId()}" name="${config.projectName()}" isExecutable="true">

    <startEvent id="StartEvent_1" name="Start"/>

    <sequenceFlow id="Flow_start_to_end" sourceRef="StartEvent_1" targetRef="EndEvent_1"/>

    <endEvent id="EndEvent_1" name="End"/>

  </process>

</definitions>
