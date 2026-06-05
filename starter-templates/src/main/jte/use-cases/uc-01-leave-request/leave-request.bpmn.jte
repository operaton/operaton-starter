@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:operaton="http://operaton.org/schema/1.0/bpmn"
             xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
             xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
             xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             targetNamespace="http://bpmn.io/schema/bpmn">

  <process id="leave-request" name="Leave Request Approval" isExecutable="true"
           operaton:historyTimeToLive="P90D">

    <startEvent id="StartEvent_1" name="Leave Requested">
      <outgoing>Flow_start_to_manager</outgoing>
    </startEvent>

    <sequenceFlow id="Flow_start_to_manager" sourceRef="StartEvent_1" targetRef="Task_ManagerReview"/>

    <userTask id="Task_ManagerReview" name="Manager Reviews Request"
              operaton:candidateGroups="managers">
      <outgoing>Flow_manager_to_gw</outgoing>
    </userTask>

    <sequenceFlow id="Flow_manager_to_gw" sourceRef="Task_ManagerReview" targetRef="Gateway_Decision"/>

    <exclusiveGateway id="Gateway_Decision" name="Approved?">
      <incoming>Flow_manager_to_gw</incoming>
      <outgoing>Flow_approved</outgoing>
      <outgoing>Flow_rejected</outgoing>
    </exclusiveGateway>

    <sequenceFlow id="Flow_approved" name="approved" sourceRef="Gateway_Decision" targetRef="Task_HRRecord">
      <conditionExpression xsi:type="tFormalExpression">${'$'}{approved == true}</conditionExpression>
    </sequenceFlow>

    <sequenceFlow id="Flow_rejected" name="rejected" sourceRef="Gateway_Decision" targetRef="Task_EmployeeNotified">
      <conditionExpression xsi:type="tFormalExpression">${'$'}{approved == false}</conditionExpression>
    </sequenceFlow>

    <userTask id="Task_HRRecord" name="HR Records Approved Leave"
              operaton:candidateGroups="hr">
      <incoming>Flow_approved</incoming>
      <outgoing>Flow_hr_to_end</outgoing>
    </userTask>

    <sequenceFlow id="Flow_hr_to_end" sourceRef="Task_HRRecord" targetRef="EndEvent_Approved"/>

    <endEvent id="EndEvent_Approved" name="Leave Approved">
      <incoming>Flow_hr_to_end</incoming>
    </endEvent>

    <userTask id="Task_EmployeeNotified" name="Employee Notified of Rejection"
              operaton:candidateGroups="employees">
      <incoming>Flow_rejected</incoming>
      <outgoing>Flow_emp_to_end</outgoing>
    </userTask>

    <sequenceFlow id="Flow_emp_to_end" sourceRef="Task_EmployeeNotified" targetRef="EndEvent_Rejected"/>

    <endEvent id="EndEvent_Rejected" name="Leave Rejected">
      <incoming>Flow_emp_to_end</incoming>
    </endEvent>

  </process>

  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="leave-request">

      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="152" y="82" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="130" y="125" width="80" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_ManagerReview_di" bpmnElement="Task_ManagerReview">
        <dc:Bounds x="240" y="60" width="120" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Gateway_Decision_di" bpmnElement="Gateway_Decision" isMarkerVisible="true">
        <dc:Bounds x="415" y="75" width="50" height="50"/>
        <bpmndi:BPMNLabel><dc:Bounds x="412" y="132" width="55" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_HRRecord_di" bpmnElement="Task_HRRecord">
        <dc:Bounds x="530" y="20" width="120" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="EndEvent_Approved_di" bpmnElement="EndEvent_Approved">
        <dc:Bounds x="712" y="42" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="690" y="85" width="80" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_EmployeeNotified_di" bpmnElement="Task_EmployeeNotified">
        <dc:Bounds x="530" y="140" width="120" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="EndEvent_Rejected_di" bpmnElement="EndEvent_Rejected">
        <dc:Bounds x="712" y="162" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="692" y="205" width="76" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNEdge id="Flow_start_to_manager_di" bpmnElement="Flow_start_to_manager">
        <di:waypoint x="188" y="100"/><di:waypoint x="240" y="100"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_manager_to_gw_di" bpmnElement="Flow_manager_to_gw">
        <di:waypoint x="360" y="100"/><di:waypoint x="415" y="100"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_approved_di" bpmnElement="Flow_approved">
        <di:waypoint x="440" y="75"/><di:waypoint x="440" y="60"/><di:waypoint x="530" y="60"/>
        <bpmndi:BPMNLabel><dc:Bounds x="456" y="43" width="47" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_rejected_di" bpmnElement="Flow_rejected">
        <di:waypoint x="440" y="125"/><di:waypoint x="440" y="180"/><di:waypoint x="530" y="180"/>
        <bpmndi:BPMNLabel><dc:Bounds x="455" y="163" width="42" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_hr_to_end_di" bpmnElement="Flow_hr_to_end">
        <di:waypoint x="650" y="60"/><di:waypoint x="712" y="60"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_emp_to_end_di" bpmnElement="Flow_emp_to_end">
        <di:waypoint x="650" y="180"/><di:waypoint x="712" y="180"/>
      </bpmndi:BPMNEdge>

    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>

</definitions>
