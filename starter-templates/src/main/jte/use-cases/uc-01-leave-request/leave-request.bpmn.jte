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
           operaton:historyTimeToLive="P90D"
           operaton:initiator="requester">

    <startEvent id="StartEvent_1" name="Leave Requested">
      <extensionElements>
        <operaton:formData>
          <operaton:formField id="startDate" label="Start date" type="date"/>
          <operaton:formField id="durationDays" label="Duration (days)" type="long"/>
        </operaton:formData>
      </extensionElements>
      <outgoing>Flow_start_to_validation</outgoing>
    </startEvent>

    <serviceTask id="Task_ValidateRequest" name="Validate Leave Request"
                 operaton:delegateExpression="${'$'}{leaveRequestValidationDelegate}">
      <incoming>Flow_start_to_validation</incoming>
      <outgoing>Flow_validation_to_gateway</outgoing>
    </serviceTask>

    <sequenceFlow id="Flow_start_to_validation" sourceRef="StartEvent_1" targetRef="Task_ValidateRequest"/>
    <sequenceFlow id="Flow_validation_to_gateway" sourceRef="Task_ValidateRequest" targetRef="Gateway_Validation"/>

    <exclusiveGateway id="Gateway_Validation" name="Request valid?">
      <incoming>Flow_validation_to_gateway</incoming>
      <outgoing>Flow_valid_request</outgoing>
      <outgoing>Flow_invalid_request</outgoing>
    </exclusiveGateway>

    <sequenceFlow id="Flow_valid_request" name="valid" sourceRef="Gateway_Validation" targetRef="Task_ManagerReview">
      <conditionExpression xsi:type="tFormalExpression">${'$'}{validationPassed == true}</conditionExpression>
    </sequenceFlow>

    <sequenceFlow id="Flow_invalid_request" name="invalid" sourceRef="Gateway_Validation" targetRef="Task_EmployeeNotified">
      <conditionExpression xsi:type="tFormalExpression">${'$'}{validationPassed == false}</conditionExpression>
    </sequenceFlow>

    <userTask id="Task_ManagerReview" name="Manager Reviews Request"
              operaton:candidateGroups="managers">
      <incoming>Flow_valid_request</incoming>
      <outgoing>Flow_manager_to_gw</outgoing>
    </userTask>

    <sequenceFlow id="Flow_manager_to_gw" sourceRef="Task_ManagerReview" targetRef="Gateway_Decision"/>

    <exclusiveGateway id="Gateway_Decision" name="Approved?" default="Flow_rejected">
      <incoming>Flow_manager_to_gw</incoming>
      <outgoing>Flow_approved</outgoing>
      <outgoing>Flow_rejected</outgoing>
    </exclusiveGateway>

    <sequenceFlow id="Flow_approved" name="approved" sourceRef="Gateway_Decision" targetRef="Task_HRRecord">
      <conditionExpression xsi:type="tFormalExpression">${'$'}{approved == true}</conditionExpression>
    </sequenceFlow>

    <sequenceFlow id="Flow_rejected" name="rejected" sourceRef="Gateway_Decision" targetRef="Task_EmployeeNotified"/>

    <userTask id="Task_HRRecord" name="HR Records Approved Leave"
              operaton:candidateGroups="hr">
      <incoming>Flow_approved</incoming>
      <outgoing>Flow_hr_to_finalize</outgoing>
    </userTask>

    <sequenceFlow id="Flow_hr_to_finalize" sourceRef="Task_HRRecord" targetRef="Task_FinalizeApproval"/>

    <serviceTask id="Task_FinalizeApproval" name="Finalize Approved Leave"
                 operaton:delegateExpression="${'$'}{finalizeLeaveApprovalDelegate}">
      <incoming>Flow_hr_to_finalize</incoming>
      <outgoing>Flow_finalize_to_end</outgoing>
    </serviceTask>

    <sequenceFlow id="Flow_finalize_to_end" sourceRef="Task_FinalizeApproval" targetRef="EndEvent_Approved"/>

    <endEvent id="EndEvent_Approved" name="Leave Approved">
      <incoming>Flow_finalize_to_end</incoming>
    </endEvent>

    <userTask id="Task_EmployeeNotified" name="Employee Notified of Rejection"
              operaton:candidateGroups="employees">
      <incoming>Flow_invalid_request</incoming>
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
        <dc:Bounds x="100" y="142" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="78" y="185" width="80" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_ValidateRequest_di" bpmnElement="Task_ValidateRequest">
        <dc:Bounds x="190" y="120" width="140" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Gateway_Validation_di" bpmnElement="Gateway_Validation" isMarkerVisible="true">
        <dc:Bounds x="385" y="135" width="50" height="50"/>
        <bpmndi:BPMNLabel><dc:Bounds x="367" y="192" width="86" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_ManagerReview_di" bpmnElement="Task_ManagerReview">
        <dc:Bounds x="500" y="40" width="140" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Gateway_Decision_di" bpmnElement="Gateway_Decision" isMarkerVisible="true">
        <dc:Bounds x="695" y="55" width="50" height="50"/>
        <bpmndi:BPMNLabel><dc:Bounds x="692" y="112" width="55" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_HRRecord_di" bpmnElement="Task_HRRecord">
        <dc:Bounds x="810" y="0" width="150" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_FinalizeApproval_di" bpmnElement="Task_FinalizeApproval">
        <dc:Bounds x="1010" y="0" width="150" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="EndEvent_Approved_di" bpmnElement="EndEvent_Approved">
        <dc:Bounds x="1215" y="22" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="1193" y="65" width="80" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_EmployeeNotified_di" bpmnElement="Task_EmployeeNotified">
        <dc:Bounds x="500" y="220" width="140" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="EndEvent_Rejected_di" bpmnElement="EndEvent_Rejected">
        <dc:Bounds x="712" y="242" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="692" y="285" width="76" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNEdge id="Flow_start_to_validation_di" bpmnElement="Flow_start_to_validation">
        <di:waypoint x="136" y="160"/><di:waypoint x="190" y="160"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_validation_to_gateway_di" bpmnElement="Flow_validation_to_gateway">
        <di:waypoint x="330" y="160"/><di:waypoint x="385" y="160"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_valid_request_di" bpmnElement="Flow_valid_request">
        <di:waypoint x="410" y="135"/><di:waypoint x="410" y="80"/><di:waypoint x="500" y="80"/>
        <bpmndi:BPMNLabel><dc:Bounds x="426" y="63" width="26" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_invalid_request_di" bpmnElement="Flow_invalid_request">
        <di:waypoint x="410" y="185"/><di:waypoint x="410" y="260"/><di:waypoint x="500" y="260"/>
        <bpmndi:BPMNLabel><dc:Bounds x="424" y="243" width="36" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_manager_to_gw_di" bpmnElement="Flow_manager_to_gw">
        <di:waypoint x="640" y="80"/><di:waypoint x="695" y="80"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_approved_di" bpmnElement="Flow_approved">
        <di:waypoint x="720" y="55"/><di:waypoint x="720" y="40"/><di:waypoint x="810" y="40"/>
        <bpmndi:BPMNLabel><dc:Bounds x="736" y="23" width="47" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_rejected_di" bpmnElement="Flow_rejected">
        <di:waypoint x="720" y="105"/><di:waypoint x="720" y="260"/><di:waypoint x="640" y="260"/>
        <bpmndi:BPMNLabel><dc:Bounds x="735" y="183" width="42" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_hr_to_finalize_di" bpmnElement="Flow_hr_to_finalize">
        <di:waypoint x="960" y="40"/><di:waypoint x="1010" y="40"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_finalize_to_end_di" bpmnElement="Flow_finalize_to_end">
        <di:waypoint x="1160" y="40"/><di:waypoint x="1215" y="40"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_emp_to_end_di" bpmnElement="Flow_emp_to_end">
        <di:waypoint x="640" y="260"/><di:waypoint x="712" y="260"/>
      </bpmndi:BPMNEdge>

    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>

</definitions>
