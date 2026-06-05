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

  <process id="order-fulfillment" name="Order Fulfillment" isExecutable="true">

    <startEvent id="StartEvent_1" name="Order Placed">
      <outgoing>Flow_start_to_inventory</outgoing>
    </startEvent>

    <serviceTask id="Task_ValidateInventory" name="Validate Inventory"
                 operaton:delegateExpression="${'$'}{inventoryDelegate}">
      <incoming>Flow_start_to_inventory</incoming>
      <outgoing>Flow_inventory_to_gw</outgoing>
    </serviceTask>

    <exclusiveGateway id="Gateway_InStock" name="In Stock?">
      <incoming>Flow_inventory_to_gw</incoming>
      <outgoing>Flow_in_stock</outgoing>
      <outgoing>Flow_out_of_stock</outgoing>
    </exclusiveGateway>

    <serviceTask id="Task_ChargePayment" name="Charge Payment"
                 operaton:delegateExpression="${'$'}{paymentDelegate}">
      <incoming>Flow_in_stock</incoming>
      <outgoing>Flow_payment_to_pack</outgoing>
    </serviceTask>

    <userTask id="Task_PackShip" name="Pack &amp; Ship"
              operaton:candidateGroups="warehouse">
      <incoming>Flow_payment_to_pack</incoming>
      <outgoing>Flow_pack_to_notify</outgoing>
    </userTask>

    <serviceTask id="Task_NotifyCustomer" name="Notify Customer"
                 operaton:delegateExpression="${'$'}{notifyCustomerDelegate}">
      <incoming>Flow_pack_to_notify</incoming>
      <outgoing>Flow_notify_to_shipped</outgoing>
    </serviceTask>

    <serviceTask id="Task_NotifyBackorder" name="Notify Backorder"
                 operaton:delegateExpression="${'$'}{notifyBackorderDelegate}">
      <incoming>Flow_out_of_stock</incoming>
      <outgoing>Flow_backorder_to_end</outgoing>
    </serviceTask>

    <endEvent id="EndEvent_Shipped" name="Order Shipped">
      <incoming>Flow_notify_to_shipped</incoming>
    </endEvent>

    <endEvent id="EndEvent_Backordered" name="Order Backordered">
      <incoming>Flow_backorder_to_end</incoming>
    </endEvent>

    <sequenceFlow id="Flow_start_to_inventory" sourceRef="StartEvent_1" targetRef="Task_ValidateInventory"/>
    <sequenceFlow id="Flow_inventory_to_gw" sourceRef="Task_ValidateInventory" targetRef="Gateway_InStock"/>
    <sequenceFlow id="Flow_in_stock" name="in stock" sourceRef="Gateway_InStock" targetRef="Task_ChargePayment">
      <conditionExpression xsi:type="tFormalExpression">${'$'}{available == true}</conditionExpression>
    </sequenceFlow>
    <sequenceFlow id="Flow_out_of_stock" name="out of stock" sourceRef="Gateway_InStock" targetRef="Task_NotifyBackorder">
      <conditionExpression xsi:type="tFormalExpression">${'$'}{available == false}</conditionExpression>
    </sequenceFlow>
    <sequenceFlow id="Flow_payment_to_pack" sourceRef="Task_ChargePayment" targetRef="Task_PackShip"/>
    <sequenceFlow id="Flow_pack_to_notify" sourceRef="Task_PackShip" targetRef="Task_NotifyCustomer"/>
    <sequenceFlow id="Flow_notify_to_shipped" sourceRef="Task_NotifyCustomer" targetRef="EndEvent_Shipped"/>
    <sequenceFlow id="Flow_backorder_to_end" sourceRef="Task_NotifyBackorder" targetRef="EndEvent_Backordered"/>

  </process>

  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="order-fulfillment">

      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="152" y="172" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="138" y="215" width="65" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_ValidateInventory_di" bpmnElement="Task_ValidateInventory">
        <dc:Bounds x="240" y="150" width="120" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Gateway_InStock_di" bpmnElement="Gateway_InStock" isMarkerVisible="true">
        <dc:Bounds x="415" y="165" width="50" height="50"/>
        <bpmndi:BPMNLabel><dc:Bounds x="410" y="222" width="60" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_ChargePayment_di" bpmnElement="Task_ChargePayment">
        <dc:Bounds x="520" y="80" width="120" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_PackShip_di" bpmnElement="Task_PackShip">
        <dc:Bounds x="700" y="80" width="120" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_NotifyCustomer_di" bpmnElement="Task_NotifyCustomer">
        <dc:Bounds x="880" y="80" width="120" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_NotifyBackorder_di" bpmnElement="Task_NotifyBackorder">
        <dc:Bounds x="520" y="250" width="120" height="80"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="EndEvent_Shipped_di" bpmnElement="EndEvent_Shipped">
        <dc:Bounds x="1062" y="102" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="1042" y="145" width="77" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="EndEvent_Backordered_di" bpmnElement="EndEvent_Backordered">
        <dc:Bounds x="702" y="272" width="36" height="36"/>
        <bpmndi:BPMNLabel><dc:Bounds x="678" y="315" width="84" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNEdge id="Flow_start_to_inventory_di" bpmnElement="Flow_start_to_inventory">
        <di:waypoint x="188" y="190"/><di:waypoint x="240" y="190"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_inventory_to_gw_di" bpmnElement="Flow_inventory_to_gw">
        <di:waypoint x="360" y="190"/><di:waypoint x="415" y="190"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_in_stock_di" bpmnElement="Flow_in_stock">
        <di:waypoint x="440" y="165"/><di:waypoint x="440" y="120"/><di:waypoint x="520" y="120"/>
        <bpmndi:BPMNLabel><dc:Bounds x="448" y="140" width="42" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_out_of_stock_di" bpmnElement="Flow_out_of_stock">
        <di:waypoint x="440" y="215"/><di:waypoint x="440" y="290"/><di:waypoint x="520" y="290"/>
        <bpmndi:BPMNLabel><dc:Bounds x="445" y="249" width="65" height="14"/></bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_payment_to_pack_di" bpmnElement="Flow_payment_to_pack">
        <di:waypoint x="640" y="120"/><di:waypoint x="700" y="120"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_pack_to_notify_di" bpmnElement="Flow_pack_to_notify">
        <di:waypoint x="820" y="120"/><di:waypoint x="880" y="120"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_notify_to_shipped_di" bpmnElement="Flow_notify_to_shipped">
        <di:waypoint x="1000" y="120"/><di:waypoint x="1062" y="120"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_backorder_to_end_di" bpmnElement="Flow_backorder_to_end">
        <di:waypoint x="640" y="290"/><di:waypoint x="702" y="290"/>
      </bpmndi:BPMNEdge>

    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>

</definitions>
