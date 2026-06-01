@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="https://www.omg.org/spec/DMN/20191111/MODEL/"
             xmlns:dmndi="https://www.omg.org/spec/DMN/20191111/DMNDI/"
             xmlns:dc="http://www.omg.org/spec/DMN/20180521/DC/"
             id="${config.artifactId()}-decisions"
             name="${config.projectName()} Decisions"
             namespace="http://operaton.org/schema/1.0/dmn">

  <decision id="skeleton-decision" name="Skeleton Decision">
    <decisionTable id="DecisionTable_1" hitPolicy="FIRST">
      <input id="input_1" label="Category">
        <inputExpression id="inputExpression_1" typeRef="string">
          <text>category</text>
        </inputExpression>
      </input>
      <output id="output_1" label="Result" name="result" typeRef="string"/>
      <rule id="rule_1">
        <description>Standard category is accepted</description>
        <inputEntry id="inputEntry_1"><text>"standard"</text></inputEntry>
        <outputEntry id="outputEntry_1"><text>"ACCEPTED"</text></outputEntry>
      </rule>
      <rule id="rule_2">
        <description>Premium category is accepted</description>
        <inputEntry id="inputEntry_2"><text>"premium"</text></inputEntry>
        <outputEntry id="outputEntry_2"><text>"ACCEPTED"</text></outputEntry>
      </rule>
      <rule id="rule_3">
        <description>All other categories are rejected</description>
        <inputEntry id="inputEntry_3"><text></text></inputEntry>
        <outputEntry id="outputEntry_3"><text>"REJECTED"</text></outputEntry>
      </rule>
    </decisionTable>
  </decision>

</definitions>
