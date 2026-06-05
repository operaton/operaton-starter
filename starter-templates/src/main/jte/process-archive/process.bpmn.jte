@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
             xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
             xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             targetNamespace="http://bpmn.io/schema/bpmn"
             exporter="Operaton Modeler"
             exporterVersion="1.0.0">

  <process id="${config.artifactId()}" name="${config.projectName()}" isExecutable="true">

    <startEvent id="StartEvent_1" name="Start"/>

    <sequenceFlow id="Flow_start_to_end" sourceRef="StartEvent_1" targetRef="EndEvent_1"/>

    <endEvent id="EndEvent_1" name="End"/>

  </process>

  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="${config.artifactId()}">
      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="152" y="82" width="36" height="36"/>
        <bpmndi:BPMNLabel>
          <dc:Bounds x="158" y="125" width="24" height="14"/>
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="EndEvent_1_di" bpmnElement="EndEvent_1">
        <dc:Bounds x="242" y="82" width="36" height="36"/>
        <bpmndi:BPMNLabel>
          <dc:Bounds x="250" y="125" width="20" height="14"/>
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_start_to_end_di" bpmnElement="Flow_start_to_end">
        <di:waypoint x="188" y="100"/>
        <di:waypoint x="242" y="100"/>
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>

</definitions>
