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
      <bpmndi:BPMNShape id="Pool_LeaveRequest_di" bpmnElement="Pool_LeaveRequest" isHorizontal="true">
        <dc:Bounds x="170" y="70" width="1310" height="680" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_System_di" bpmnElement="Lane_System" isHorizontal="true">
        <dc:Bounds x="200" y="410" width="1280" height="340" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_HR_di" bpmnElement="Lane_HR" isHorizontal="true">
        <dc:Bounds x="200" y="290" width="1280" height="120" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_Managers_di" bpmnElement="Lane_Managers" isHorizontal="true">
        <dc:Bounds x="200" y="170" width="1280" height="120" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_Employees_di" bpmnElement="Lane_Employees" isHorizontal="true">
        <dc:Bounds x="200" y="70" width="1280" height="100" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="EndEvent_Rejected_di" bpmnElement="EndEvent_Rejected">
        <dc:Bounds x="1172" y="572" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1152" y="615" width="77" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="EndEvent_Approved_di" bpmnElement="EndEvent_Approved">
        <dc:Bounds x="1292" y="472" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1270" y="515" width="80" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Task_SendRejectionEmail_di" bpmnElement="Task_SendRejectionEmail">
        <dc:Bounds x="970" y="550" width="140" height="80" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Task_FinalizeApproval_di" bpmnElement="Task_FinalizeApproval">
        <dc:Bounds x="1090" y="450" width="140" height="80" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Task_HRRecord_di" bpmnElement="Task_HRRecord">
        <dc:Bounds x="910" y="310" width="140" height="80" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_Decision_di" bpmnElement="Gateway_Decision" isMarkerVisible="true">
        <dc:Bounds x="805" y="465" width="50" height="50" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="804" y="522" width="54" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="EndEvent_EscalationSent_di" bpmnElement="EndEvent_EscalationSent">
        <dc:Bounds x="832" y="572" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="813" y="615" width="74" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Task_EscalationReminder_di" bpmnElement="Task_EscalationReminder">
        <dc:Bounds x="630" y="550" width="140" height="80" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Task_ManagerReview_di" bpmnElement="Task_ManagerReview">
        <dc:Bounds x="600" y="190" width="140" height="80" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_Validation_di" bpmnElement="Gateway_Validation" isMarkerVisible="true">
        <dc:Bounds x="515" y="465" width="50" height="50" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="575" y="483" width="73" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Task_ValidateRequest_di" bpmnElement="Task_ValidateRequest">
        <dc:Bounds x="320" y="450" width="140" height="80" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="262" y="102" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="296" y="145" width="87" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="TimerBoundary_ManagerReview_di" bpmnElement="TimerBoundary_ManagerReview">
        <dc:Bounds x="664" y="252" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="643" y="295" width="79" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_rejection_to_end_di" bpmnElement="Flow_rejection_to_end">
        <di:waypoint x="1110" y="590" />
        <di:waypoint x="1172" y="590" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_finalize_to_end_di" bpmnElement="Flow_finalize_to_end">
        <di:waypoint x="1230" y="490" />
        <di:waypoint x="1292" y="490" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_rejected_di" bpmnElement="Flow_rejected">
        <di:waypoint x="855" y="490" />
        <di:waypoint x="1040" y="490" />
        <di:waypoint x="1040" y="550" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="925" y="470" width="40" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_invalid_request_di" bpmnElement="Flow_invalid_request">
        <di:waypoint x="540" y="515" />
        <di:waypoint x="540" y="680" />
        <di:waypoint x="1040" y="680" />
        <di:waypoint x="1040" y="630" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="552" y="589" width="32" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_hr_to_finalize_di" bpmnElement="Flow_hr_to_finalize">
        <di:waypoint x="1050" y="350" />
        <di:waypoint x="1160" y="350" />
        <di:waypoint x="1160" y="450" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_approved_di" bpmnElement="Flow_approved">
        <di:waypoint x="830" y="465" />
        <di:waypoint x="830" y="350" />
        <di:waypoint x="910" y="350" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="837" y="443" width="46" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_manager_to_gw_di" bpmnElement="Flow_manager_to_gw">
        <di:waypoint x="740" y="230" />
        <di:waypoint x="780" y="230" />
        <di:waypoint x="780" y="490" />
        <di:waypoint x="805" y="490" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_reminder_to_end_di" bpmnElement="Flow_reminder_to_end">
        <di:waypoint x="770" y="590" />
        <di:waypoint x="832" y="590" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_timer_to_reminder_di" bpmnElement="Flow_timer_to_reminder">
        <di:waypoint x="682" y="288" />
        <di:waypoint x="682" y="550" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_valid_request_di" bpmnElement="Flow_valid_request">
        <di:waypoint x="540" y="465" />
        <di:waypoint x="540" y="230" />
        <di:waypoint x="600" y="230" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="554" y="343" width="23" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_validation_to_gateway_di" bpmnElement="Flow_validation_to_gateway">
        <di:waypoint x="460" y="490" />
        <di:waypoint x="515" y="490" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_start_to_validation_di" bpmnElement="Flow_start_to_validation">
        <di:waypoint x="280" y="138" />
        <di:waypoint x="280" y="490" />
        <di:waypoint x="320" y="490" />
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>

</definitions>
