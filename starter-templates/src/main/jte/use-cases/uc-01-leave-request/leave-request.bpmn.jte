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

  <collaboration id="Collaboration_LeaveRequest">
    <participant id="Pool_LeaveRequest" name="Leave Request Approval" processRef="leave-request"/>
  </collaboration>

  <process id="leave-request" name="Leave Request Approval" isExecutable="true"
           operaton:historyTimeToLive="P90D"
           operaton:candidateStarterGroups="employees,operaton-admin">

    <laneSet id="LaneSet_1">
      <lane id="Lane_Employees" name="employees">
        <flowNodeRef>StartEvent_1</flowNodeRef>
      </lane>
      <lane id="Lane_Managers" name="managers">
        <flowNodeRef>Task_ManagerReview</flowNodeRef>
      </lane>
      <lane id="Lane_HR" name="hr">
        <flowNodeRef>Task_HRRecord</flowNodeRef>
      </lane>
      <lane id="Lane_System" name="System">
        <flowNodeRef>Task_ValidateRequest</flowNodeRef>
        <flowNodeRef>Gateway_Validation</flowNodeRef>
        <flowNodeRef>Gateway_Decision</flowNodeRef>
        <flowNodeRef>Task_EscalationReminder</flowNodeRef>
        <flowNodeRef>Task_FinalizeApproval</flowNodeRef>
        <flowNodeRef>Task_SendRejectionEmail</flowNodeRef>
        <flowNodeRef>EndEvent_Approved</flowNodeRef>
        <flowNodeRef>EndEvent_Rejected</flowNodeRef>
        <flowNodeRef>EndEvent_EscalationSent</flowNodeRef>
      </lane>
    </laneSet>

    <startEvent id="StartEvent_1" name="Leave Requested" operaton:initiator="requester">
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

    <exclusiveGateway id="Gateway_Validation" name="Request valid?">
      <incoming>Flow_validation_to_gateway</incoming>
      <outgoing>Flow_valid_request</outgoing>
      <outgoing>Flow_invalid_request</outgoing>
    </exclusiveGateway>

    <userTask id="Task_ManagerReview" name="Review Request"
              operaton:candidateGroups="managers"
              operaton:formKey="embedded:app:forms/manager-review-form.html">
      <extensionElements>
        <operaton:formData>
          <operaton:formField id="requester" label="Requester" type="string">
            <operaton:properties><operaton:property id="readonly" value="true"/></operaton:properties>
          </operaton:formField>
          <operaton:formField id="startDate" label="Start Date" type="string">
            <operaton:properties><operaton:property id="readonly" value="true"/></operaton:properties>
          </operaton:formField>
          <operaton:formField id="endDate" label="End Date" type="string">
            <operaton:properties><operaton:property id="readonly" value="true"/></operaton:properties>
          </operaton:formField>
          <operaton:formField id="durationDays" label="Days Requested" type="long">
            <operaton:properties><operaton:property id="readonly" value="true"/></operaton:properties>
          </operaton:formField>
          <operaton:formField id="remainingVacationDays" label="Remaining Vacation Days" type="long">
            <operaton:properties><operaton:property id="readonly" value="true"/></operaton:properties>
          </operaton:formField>
          <operaton:formField id="approved" label="Approved?" type="boolean"/>
        </operaton:formData>
      </extensionElements>
      <incoming>Flow_valid_request</incoming>
      <outgoing>Flow_manager_to_gw</outgoing>
    </userTask>

    <!-- Non-interrupting timer boundary: escalation reminder if manager does not respond -->
    <boundaryEvent id="TimerBoundary_ManagerReview" name="Review Timeout"
                   attachedToRef="Task_ManagerReview" cancelActivity="false">
      <outgoing>Flow_timer_to_reminder</outgoing>
      <timerEventDefinition id="TimerDef_1">
        <timeDuration xsi:type="tFormalExpression">${'$'}{managerReviewTimeout != null ? managerReviewTimeout : "PT72H"}</timeDuration>
      </timerEventDefinition>
    </boundaryEvent>

    <serviceTask id="Task_EscalationReminder" name="Send Escalation Reminder"
                 operaton:delegateExpression="${'$'}{escalationReminderDelegate}">
      <incoming>Flow_timer_to_reminder</incoming>
      <outgoing>Flow_reminder_to_end</outgoing>
    </serviceTask>

    <endEvent id="EndEvent_EscalationSent" name="Reminder Sent">
      <incoming>Flow_reminder_to_end</incoming>
    </endEvent>

    <exclusiveGateway id="Gateway_Decision" name="Approved?" default="Flow_rejected">
      <incoming>Flow_manager_to_gw</incoming>
      <outgoing>Flow_approved</outgoing>
      <outgoing>Flow_rejected</outgoing>
    </exclusiveGateway>

    <userTask id="Task_HRRecord" name="Record Approved Leave"
              operaton:candidateGroups="hr"
              operaton:formKey="embedded:app:forms/hr-record-form.html">
      <extensionElements>
        <operaton:formData>
          <operaton:formField id="requester" label="Requester" type="string">
            <operaton:properties><operaton:property id="readonly" value="true"/></operaton:properties>
          </operaton:formField>
          <operaton:formField id="startDate" label="Start Date" type="string">
            <operaton:properties><operaton:property id="readonly" value="true"/></operaton:properties>
          </operaton:formField>
          <operaton:formField id="endDate" label="End Date" type="string">
            <operaton:properties><operaton:property id="readonly" value="true"/></operaton:properties>
          </operaton:formField>
          <operaton:formField id="durationDays" label="Days Requested" type="long">
            <operaton:properties><operaton:property id="readonly" value="true"/></operaton:properties>
          </operaton:formField>
          <operaton:formField id="remainingVacationDays" label="Remaining Vacation Days" type="long">
            <operaton:properties><operaton:property id="readonly" value="true"/></operaton:properties>
          </operaton:formField>
        </operaton:formData>
      </extensionElements>
      <incoming>Flow_approved</incoming>
      <outgoing>Flow_hr_to_finalize</outgoing>
    </userTask>

    <serviceTask id="Task_FinalizeApproval" name="Finalize Approved Leave"
                 operaton:delegateExpression="${'$'}{finalizeLeaveApprovalDelegate}">
      <incoming>Flow_hr_to_finalize</incoming>
      <outgoing>Flow_finalize_to_end</outgoing>
    </serviceTask>

    <sendTask id="Task_SendRejectionEmail" name="Send Rejection Email"
              operaton:delegateExpression="${'$'}{leaveRejectionEmailDelegate}">
      <incoming>Flow_invalid_request</incoming>
      <incoming>Flow_rejected</incoming>
      <outgoing>Flow_rejection_to_end</outgoing>
    </sendTask>

    <endEvent id="EndEvent_Approved" name="Leave Approved">
      <incoming>Flow_finalize_to_end</incoming>
    </endEvent>

    <endEvent id="EndEvent_Rejected" name="Leave Rejected">
      <incoming>Flow_rejection_to_end</incoming>
    </endEvent>

    <sequenceFlow id="Flow_start_to_validation" sourceRef="StartEvent_1" targetRef="Task_ValidateRequest"/>
    <sequenceFlow id="Flow_validation_to_gateway" sourceRef="Task_ValidateRequest" targetRef="Gateway_Validation"/>
    <sequenceFlow id="Flow_valid_request" name="valid" sourceRef="Gateway_Validation" targetRef="Task_ManagerReview">
      <conditionExpression xsi:type="tFormalExpression">${'$'}{validationPassed == true}</conditionExpression>
    </sequenceFlow>
    <sequenceFlow id="Flow_invalid_request" name="invalid" sourceRef="Gateway_Validation" targetRef="Task_SendRejectionEmail">
      <conditionExpression xsi:type="tFormalExpression">${'$'}{validationPassed == false}</conditionExpression>
    </sequenceFlow>
    <sequenceFlow id="Flow_manager_to_gw" sourceRef="Task_ManagerReview" targetRef="Gateway_Decision"/>
    <sequenceFlow id="Flow_approved" name="approved" sourceRef="Gateway_Decision" targetRef="Task_HRRecord">
      <conditionExpression xsi:type="tFormalExpression">${'$'}{approved == true}</conditionExpression>
    </sequenceFlow>
    <sequenceFlow id="Flow_rejected" name="rejected" sourceRef="Gateway_Decision" targetRef="Task_SendRejectionEmail">
      <extensionElements>
        <operaton:executionListener event="take" expression="${'$'}{execution.setVariable('leaveStatus', 'REJECTED')}"/>
      </extensionElements>
    </sequenceFlow>
    <sequenceFlow id="Flow_hr_to_finalize" sourceRef="Task_HRRecord" targetRef="Task_FinalizeApproval"/>
    <sequenceFlow id="Flow_finalize_to_end" sourceRef="Task_FinalizeApproval" targetRef="EndEvent_Approved"/>
    <sequenceFlow id="Flow_rejection_to_end" sourceRef="Task_SendRejectionEmail" targetRef="EndEvent_Rejected"/>
    <sequenceFlow id="Flow_timer_to_reminder" sourceRef="TimerBoundary_ManagerReview" targetRef="Task_EscalationReminder"/>
    <sequenceFlow id="Flow_reminder_to_end" sourceRef="Task_EscalationReminder" targetRef="EndEvent_EscalationSent"/>

  </process>

  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Collaboration_LeaveRequest">

      <!-- Pool -->
      <bpmndi:BPMNShape id="Pool_LeaveRequest_di" bpmnElement="Pool_LeaveRequest" isHorizontal="true">
        <dc:Bounds x="160" y="40" width="1420" height="680"/>
      </bpmndi:BPMNShape>

      <!-- Lane: employees (y=40, h=100) -->
      <bpmndi:BPMNShape id="Lane_Employees_di" bpmnElement="Lane_Employees" isHorizontal="true">
        <dc:Bounds x="190" y="40" width="1390" height="100"/>
      </bpmndi:BPMNShape>

      <!-- Lane: managers (y=140, h=120) -->
      <bpmndi:BPMNShape id="Lane_Managers_di" bpmnElement="Lane_Managers" isHorizontal="true">
        <dc:Bounds x="190" y="140" width="1390" height="120"/>
      </bpmndi:BPMNShape>

      <!-- Lane: hr (y=260, h=120) -->
      <bpmndi:BPMNShape id="Lane_HR_di" bpmnElement="Lane_HR" isHorizontal="true">
        <dc:Bounds x="190" y="260" width="1390" height="120"/>
      </bpmndi:BPMNShape>

      <!-- Lane: System (y=380, h=340) -->
      <bpmndi:BPMNShape id="Lane_System_di" bpmnElement="Lane_System" isHorizontal="true">
        <dc:Bounds x="190" y="380" width="1390" height="340"/>
      </bpmndi:BPMNShape>

      <!-- StartEvent (employees lane, center y=90) -->
      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="252" y="72" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="230" y="115" width="80" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <!-- Task_ManagerReview (managers lane, center y=200) -->
      <bpmndi:BPMNShape id="Task_ManagerReview_di" bpmnElement="Task_ManagerReview">
        <dc:Bounds x="590" y="160" width="140" height="80"/>
      </bpmndi:BPMNShape>

      <!-- TimerBoundary on ManagerReview (bottom-right of task) -->
      <bpmndi:BPMNShape id="TimerBoundary_ManagerReview_di" bpmnElement="TimerBoundary_ManagerReview">
        <dc:Bounds x="654" y="222" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="632" y="265" width="80" height="27"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <!-- Task_HRRecord (hr lane, center y=320) -->
      <bpmndi:BPMNShape id="Task_HRRecord_di" bpmnElement="Task_HRRecord">
        <dc:Bounds x="900" y="280" width="140" height="80"/>
      </bpmndi:BPMNShape>

      <!-- System lane elements (center y=550) -->
      <bpmndi:BPMNShape id="Task_ValidateRequest_di" bpmnElement="Task_ValidateRequest">
        <dc:Bounds x="310" y="420" width="140" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Gateway_Validation_di" bpmnElement="Gateway_Validation" isMarkerVisible="true">
        <dc:Bounds x="505" y="435" width="50" height="50"/>
        <bpmndi:BPMNLabel><dc:Bounds x="488" y="492" width="85" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_EscalationReminder_di" bpmnElement="Task_EscalationReminder">
        <dc:Bounds x="620" y="520" width="140" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Gateway_Decision_di" bpmnElement="Gateway_Decision" isMarkerVisible="true">
        <dc:Bounds x="795" y="435" width="50" height="50"/>
        <bpmndi:BPMNLabel><dc:Bounds x="793" y="492" width="55" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_FinalizeApproval_di" bpmnElement="Task_FinalizeApproval">
        <dc:Bounds x="1080" y="420" width="140" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_SendRejectionEmail_di" bpmnElement="Task_SendRejectionEmail">
        <dc:Bounds x="960" y="520" width="140" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="EndEvent_Approved_di" bpmnElement="EndEvent_Approved">
        <dc:Bounds x="1282" y="442" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="1260" y="485" width="80" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="EndEvent_Rejected_di" bpmnElement="EndEvent_Rejected">
        <dc:Bounds x="1162" y="542" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="1141" y="585" width="78" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="EndEvent_EscalationSent_di" bpmnElement="EndEvent_EscalationSent">
        <dc:Bounds x="822" y="542" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="802" y="585" width="76" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <!-- Edges -->
      <bpmndi:BPMNEdge id="Flow_start_to_validation_di" bpmnElement="Flow_start_to_validation">
        <di:waypoint x="270" y="108"/><di:waypoint x="270" y="460"/><di:waypoint x="310" y="460"/>
      </bpmndi:BPMNEdge>

      <bpmndi:BPMNEdge id="Flow_validation_to_gateway_di" bpmnElement="Flow_validation_to_gateway">
        <di:waypoint x="450" y="460"/><di:waypoint x="505" y="460"/>
      </bpmndi:BPMNEdge>

      <bpmndi:BPMNEdge id="Flow_valid_request_di" bpmnElement="Flow_valid_request">
        <di:waypoint x="530" y="435"/><di:waypoint x="530" y="200"/><di:waypoint x="590" y="200"/>
        <bpmndi:BPMNLabel><dc:Bounds x="542" y="313" width="26" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>

      <bpmndi:BPMNEdge id="Flow_invalid_request_di" bpmnElement="Flow_invalid_request">
        <di:waypoint x="530" y="485"/><di:waypoint x="530" y="560"/><di:waypoint x="960" y="560"/>
        <bpmndi:BPMNLabel><dc:Bounds x="540" y="515" width="36" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>

      <bpmndi:BPMNEdge id="Flow_manager_to_gw_di" bpmnElement="Flow_manager_to_gw">
        <di:waypoint x="730" y="200"/><di:waypoint x="820" y="200"/><di:waypoint x="820" y="435"/>
      </bpmndi:BPMNEdge>

      <bpmndi:BPMNEdge id="Flow_approved_di" bpmnElement="Flow_approved">
        <di:waypoint x="820" y="435"/><di:waypoint x="820" y="320"/><di:waypoint x="900" y="320"/>
        <bpmndi:BPMNLabel><dc:Bounds x="836" y="373" width="47" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>

      <bpmndi:BPMNEdge id="Flow_rejected_di" bpmnElement="Flow_rejected">
        <di:waypoint x="845" y="460"/><di:waypoint x="960" y="460"/><di:waypoint x="960" y="520"/>
        <bpmndi:BPMNLabel><dc:Bounds x="880" y="440" width="42" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>

      <bpmndi:BPMNEdge id="Flow_hr_to_finalize_di" bpmnElement="Flow_hr_to_finalize">
        <di:waypoint x="1040" y="320"/><di:waypoint x="1150" y="320"/><di:waypoint x="1150" y="460"/><di:waypoint x="1080" y="460"/>
      </bpmndi:BPMNEdge>

      <bpmndi:BPMNEdge id="Flow_finalize_to_end_di" bpmnElement="Flow_finalize_to_end">
        <di:waypoint x="1220" y="460"/><di:waypoint x="1282" y="460"/>
      </bpmndi:BPMNEdge>

      <bpmndi:BPMNEdge id="Flow_rejection_to_end_di" bpmnElement="Flow_rejection_to_end">
        <di:waypoint x="1100" y="560"/><di:waypoint x="1162" y="560"/>
      </bpmndi:BPMNEdge>

      <bpmndi:BPMNEdge id="Flow_timer_to_reminder_di" bpmnElement="Flow_timer_to_reminder">
        <di:waypoint x="672" y="258"/><di:waypoint x="672" y="560"/><di:waypoint x="620" y="560"/>
      </bpmndi:BPMNEdge>

      <bpmndi:BPMNEdge id="Flow_reminder_to_end_di" bpmnElement="Flow_reminder_to_end">
        <di:waypoint x="760" y="560"/><di:waypoint x="822" y="560"/>
      </bpmndi:BPMNEdge>

    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>

</definitions>
