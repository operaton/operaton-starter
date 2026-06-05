@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xmlns:operaton="http://operaton.org/schema/1.0/bpmn"
             xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
             xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
             xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
             targetNamespace="http://www.operaton.org/examples"
             exporter="operaton modeler"
             exporterVersion="1.0">

  <process id="incident-management" name="Incident Management" isExecutable="true">

    <startEvent id="StartEvent_1" name="Incident Reported">
      <outgoing>Flow_start_to_triage</outgoing>
    </startEvent>

    <userTask id="Task_FirstLineTriage" name="First-Line Triage"
              operaton:candidateGroups="first-line">
      <incoming>Flow_start_to_triage</incoming>
      <outgoing>Flow_triage_to_gw</outgoing>
    </userTask>

    <!-- Interrupting boundary timer: escalates to second-line after SLA duration -->
    <boundaryEvent id="BoundaryTimer_Escalate" name="SLA Timer"
                   cancelActivity="true"
                   attachedToRef="Task_FirstLineTriage">
      <outgoing>Flow_timer_to_secondline</outgoing>
      <timerEventDefinition id="TimerEventDefinition_1">
        <timeDuration xsi:type="tFormalExpression">${'$'}{timerDuration}</timeDuration>
      </timerEventDefinition>
    </boundaryEvent>

    <exclusiveGateway id="Gateway_Resolved" name="Resolved?">
      <incoming>Flow_triage_to_gw</incoming>
      <outgoing>Flow_resolved</outgoing>
      <outgoing>Flow_not_resolved</outgoing>
    </exclusiveGateway>

    <serviceTask id="Task_CloseTicket" name="Close Ticket"
                 operaton:class="${config.javaPackage()}.delegate.CloseTicketDelegate">
      <incoming>Flow_resolved</incoming>
      <outgoing>Flow_close_end</outgoing>
    </serviceTask>

    <userTask id="Task_SecondLine" name="Second-Line Engineer"
              operaton:candidateGroups="second-line">
      <incoming>Flow_timer_to_secondline</incoming>
      <incoming>Flow_not_resolved</incoming>
      <outgoing>Flow_secondline_postmortem</outgoing>
    </userTask>

    <serviceTask id="Task_PostMortem" name="Post-Mortem Notify"
                 operaton:class="${config.javaPackage()}.delegate.PostMortemDelegate">
      <incoming>Flow_secondline_postmortem</incoming>
      <outgoing>Flow_postmortem_end</outgoing>
    </serviceTask>

    <endEvent id="EndEvent_Closed" name="Ticket Closed">
      <incoming>Flow_close_end</incoming>
    </endEvent>

    <endEvent id="EndEvent_PostMortem" name="Post-Mortem Done">
      <incoming>Flow_postmortem_end</incoming>
    </endEvent>

    <sequenceFlow id="Flow_start_to_triage" sourceRef="StartEvent_1" targetRef="Task_FirstLineTriage"/>
    <sequenceFlow id="Flow_triage_to_gw" sourceRef="Task_FirstLineTriage" targetRef="Gateway_Resolved"/>
    <sequenceFlow id="Flow_timer_to_secondline" sourceRef="BoundaryTimer_Escalate" targetRef="Task_SecondLine"/>
    <sequenceFlow id="Flow_resolved" name="resolved" sourceRef="Gateway_Resolved" targetRef="Task_CloseTicket">
      <conditionExpression xsi:type="tFormalExpression">${'$'}{resolved == true}</conditionExpression>
    </sequenceFlow>
    <sequenceFlow id="Flow_not_resolved" name="not resolved" sourceRef="Gateway_Resolved" targetRef="Task_SecondLine">
      <conditionExpression xsi:type="tFormalExpression">${'$'}{resolved != true}</conditionExpression>
    </sequenceFlow>
    <sequenceFlow id="Flow_close_end" sourceRef="Task_CloseTicket" targetRef="EndEvent_Closed"/>
    <sequenceFlow id="Flow_secondline_postmortem" sourceRef="Task_SecondLine" targetRef="Task_PostMortem"/>
    <sequenceFlow id="Flow_postmortem_end" sourceRef="Task_PostMortem" targetRef="EndEvent_PostMortem"/>

  </process>

  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="incident-management">

      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="152" y="172" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="131" y="215" width="79" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_FirstLineTriage_di" bpmnElement="Task_FirstLineTriage">
        <dc:Bounds x="240" y="150" width="120" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="BoundaryTimer_Escalate_di" bpmnElement="BoundaryTimer_Escalate">
        <dc:Bounds x="282" y="212" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="263" y="255" width="74" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Gateway_Resolved_di" bpmnElement="Gateway_Resolved" isMarkerVisible="true">
        <dc:Bounds x="415" y="165" width="50" height="50"/>
        <bpmndi:BPMNLabel><dc:Bounds x="408" y="222" width="65" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_CloseTicket_di" bpmnElement="Task_CloseTicket">
        <dc:Bounds x="520" y="80" width="120" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_SecondLine_di" bpmnElement="Task_SecondLine">
        <dc:Bounds x="520" y="190" width="120" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_PostMortem_di" bpmnElement="Task_PostMortem">
        <dc:Bounds x="700" y="190" width="120" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="EndEvent_Closed_di" bpmnElement="EndEvent_Closed">
        <dc:Bounds x="702" y="102" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="683" y="145" width="74" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="EndEvent_PostMortem_di" bpmnElement="EndEvent_PostMortem">
        <dc:Bounds x="882" y="212" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="858" y="255" width="84" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNEdge id="Flow_start_to_triage_di" bpmnElement="Flow_start_to_triage">
        <di:waypoint x="188" y="190"/><di:waypoint x="240" y="190"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_triage_to_gw_di" bpmnElement="Flow_triage_to_gw">
        <di:waypoint x="360" y="190"/><di:waypoint x="415" y="190"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_timer_to_secondline_di" bpmnElement="Flow_timer_to_secondline">
        <di:waypoint x="300" y="248"/><di:waypoint x="300" y="300"/><di:waypoint x="580" y="300"/><di:waypoint x="580" y="270"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_resolved_di" bpmnElement="Flow_resolved">
        <di:waypoint x="440" y="165"/><di:waypoint x="440" y="120"/><di:waypoint x="520" y="120"/>
        <bpmndi:BPMNLabel><dc:Bounds x="449" y="140" width="47" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_not_resolved_di" bpmnElement="Flow_not_resolved">
        <di:waypoint x="465" y="190"/><di:waypoint x="520" y="230"/>
        <bpmndi:BPMNLabel><dc:Bounds x="460" y="215" width="65" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_close_end_di" bpmnElement="Flow_close_end">
        <di:waypoint x="640" y="120"/><di:waypoint x="702" y="120"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_secondline_postmortem_di" bpmnElement="Flow_secondline_postmortem">
        <di:waypoint x="640" y="230"/><di:waypoint x="700" y="230"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_postmortem_end_di" bpmnElement="Flow_postmortem_end">
        <di:waypoint x="820" y="230"/><di:waypoint x="882" y="230"/>
      </bpmndi:BPMNEdge>

    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>

</definitions>
