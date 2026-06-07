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

  <process id="loan-application" name="Loan Application" isExecutable="true"
           operaton:candidateStarterGroups="employees,operaton-admin">

    <startEvent id="StartEvent_1" name="Loan Application Received">
      <outgoing>Flow_start_to_credit</outgoing>
    </startEvent>

    <serviceTask id="Task_CreditScore" name="Check Credit Score"
                 operaton:class="${config.javaPackage()}.delegate.CreditScoreDelegate">
      <incoming>Flow_start_to_credit</incoming>
      <outgoing>Flow_credit_to_dmn</outgoing>
    </serviceTask>

    <businessRuleTask id="Task_RiskAssessment" name="Assess Risk (DMN)"
                      operaton:decisionRef="risk-assessment"
                      operaton:decisionRefBinding="latest"
                      operaton:mapDecisionResult="singleEntry"
                      operaton:resultVariable="riskLevel">
      <incoming>Flow_credit_to_dmn</incoming>
      <outgoing>Flow_dmn_to_gw</outgoing>
    </businessRuleTask>

    <exclusiveGateway id="Gateway_Risk" name="Risk Level?">
      <incoming>Flow_dmn_to_gw</incoming>
      <outgoing>Flow_low</outgoing>
      <outgoing>Flow_medium</outgoing>
      <outgoing>Flow_high</outgoing>
    </exclusiveGateway>

    <serviceTask id="Task_AutoApprove" name="Auto-Approve Notify"
                 operaton:class="${config.javaPackage()}.delegate.NotificationDelegate">
      <incoming>Flow_low</incoming>
      <outgoing>Flow_approve_end</outgoing>
    </serviceTask>

    <userTask id="Task_UnderwriterReview" name="Underwriter Review"
              operaton:candidateGroups="underwriters">
      <incoming>Flow_medium</incoming>
      <outgoing>Flow_medium_end</outgoing>
    </userTask>

    <sendTask id="Task_AutoReject" name="Send Rejection Email"
              operaton:delegateExpression="${'$'}{rejectionEmailDelegate}">
      <incoming>Flow_high</incoming>
      <outgoing>Flow_reject_end</outgoing>
    </sendTask>

    <endEvent id="EndEvent_Approved" name="Loan Approved">
      <incoming>Flow_approve_end</incoming>
    </endEvent>

    <endEvent id="EndEvent_UnderwriterPending" name="Pending Underwriter Decision">
      <incoming>Flow_medium_end</incoming>
    </endEvent>

    <endEvent id="EndEvent_Rejected" name="Loan Rejected">
      <incoming>Flow_reject_end</incoming>
    </endEvent>

    <sequenceFlow id="Flow_start_to_credit" sourceRef="StartEvent_1" targetRef="Task_CreditScore"/>
    <sequenceFlow id="Flow_credit_to_dmn" sourceRef="Task_CreditScore" targetRef="Task_RiskAssessment"/>
    <sequenceFlow id="Flow_dmn_to_gw" sourceRef="Task_RiskAssessment" targetRef="Gateway_Risk"/>
    <sequenceFlow id="Flow_low" name="low" sourceRef="Gateway_Risk" targetRef="Task_AutoApprove">
      <conditionExpression xsi:type="tFormalExpression">${'$'}{riskLevel == 'low'}</conditionExpression>
    </sequenceFlow>
    <sequenceFlow id="Flow_medium" name="medium" sourceRef="Gateway_Risk" targetRef="Task_UnderwriterReview">
      <conditionExpression xsi:type="tFormalExpression">${'$'}{riskLevel == 'medium'}</conditionExpression>
    </sequenceFlow>
    <sequenceFlow id="Flow_high" name="high" sourceRef="Gateway_Risk" targetRef="Task_AutoReject">
      <conditionExpression xsi:type="tFormalExpression">${'$'}{riskLevel == 'high'}</conditionExpression>
    </sequenceFlow>
    <sequenceFlow id="Flow_approve_end" sourceRef="Task_AutoApprove" targetRef="EndEvent_Approved"/>
    <sequenceFlow id="Flow_medium_end" sourceRef="Task_UnderwriterReview" targetRef="EndEvent_UnderwriterPending">
      <extensionElements>
        <operaton:executionListener event="take" expression="${'$'}{execution.setVariable('loanDecision', 'APPROVED')}"/>
      </extensionElements>
    </sequenceFlow>
    <sequenceFlow id="Flow_reject_end" sourceRef="Task_AutoReject" targetRef="EndEvent_Rejected"/>

  </process>

  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="loan-application">

      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="152" y="152" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="128" y="195" width="85" height="27"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_CreditScore_di" bpmnElement="Task_CreditScore">
        <dc:Bounds x="240" y="130" width="120" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_RiskAssessment_di" bpmnElement="Task_RiskAssessment">
        <dc:Bounds x="410" y="130" width="120" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Gateway_Risk_di" bpmnElement="Gateway_Risk" isMarkerVisible="true">
        <dc:Bounds x="585" y="145" width="50" height="50"/>
        <bpmndi:BPMNLabel><dc:Bounds x="575" y="202" width="70" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_AutoApprove_di" bpmnElement="Task_AutoApprove">
        <dc:Bounds x="690" y="60" width="120" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_UnderwriterReview_di" bpmnElement="Task_UnderwriterReview">
        <dc:Bounds x="690" y="130" width="120" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_AutoReject_di" bpmnElement="Task_AutoReject">
        <dc:Bounds x="690" y="210" width="120" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="EndEvent_Approved_di" bpmnElement="EndEvent_Approved">
        <dc:Bounds x="872" y="82" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="853" y="125" width="74" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="EndEvent_UnderwriterPending_di" bpmnElement="EndEvent_UnderwriterPending">
        <dc:Bounds x="872" y="152" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="848" y="195" width="85" height="27"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="EndEvent_Rejected_di" bpmnElement="EndEvent_Rejected">
        <dc:Bounds x="872" y="232" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="854" y="275" width="72" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNEdge id="Flow_start_to_credit_di" bpmnElement="Flow_start_to_credit">
        <di:waypoint x="188" y="170"/><di:waypoint x="240" y="170"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_credit_to_dmn_di" bpmnElement="Flow_credit_to_dmn">
        <di:waypoint x="360" y="170"/><di:waypoint x="410" y="170"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_dmn_to_gw_di" bpmnElement="Flow_dmn_to_gw">
        <di:waypoint x="530" y="170"/><di:waypoint x="585" y="170"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_low_di" bpmnElement="Flow_low">
        <di:waypoint x="610" y="145"/><di:waypoint x="610" y="100"/><di:waypoint x="690" y="100"/>
        <bpmndi:BPMNLabel><dc:Bounds x="614" y="120" width="21" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_medium_di" bpmnElement="Flow_medium">
        <di:waypoint x="635" y="170"/><di:waypoint x="690" y="170"/>
        <bpmndi:BPMNLabel><dc:Bounds x="641" y="152" width="43" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_high_di" bpmnElement="Flow_high">
        <di:waypoint x="610" y="195"/><di:waypoint x="610" y="250"/><di:waypoint x="690" y="250"/>
        <bpmndi:BPMNLabel><dc:Bounds x="617" y="220" width="23" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_approve_end_di" bpmnElement="Flow_approve_end">
        <di:waypoint x="810" y="100"/><di:waypoint x="872" y="100"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_medium_end_di" bpmnElement="Flow_medium_end">
        <di:waypoint x="810" y="170"/><di:waypoint x="872" y="170"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_reject_end_di" bpmnElement="Flow_reject_end">
        <di:waypoint x="810" y="250"/><di:waypoint x="872" y="250"/>
      </bpmndi:BPMNEdge>

    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>

</definitions>
