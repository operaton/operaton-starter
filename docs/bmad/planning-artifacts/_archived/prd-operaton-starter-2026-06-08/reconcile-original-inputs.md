# Reconciliation: Original User Inputs vs PRD Draft

## Input 1 — Example Project Coordinates

Gaps:
- FR-1.3 adds "freely editable" for groupId/artifactId, but the input only specifies defaults — editability is an interpretation not stated by the user.
- FR-1.4 sets default version to "1.0.0-SNAPSHOT"; input says "configurable" but does not specify a default value — the PRD invents a default without basis.

## Input 2 — "Configure Now" Flow

Gaps:
- FR-2.4 proposes using a `projectType` query parameter to distinguish entry paths; input says nothing about query parameters — this is an implementation assumption that should be marked as a design decision, not a requirement.
- Input explicitly states "project type shall be configurable" when coming via "Configure Now" — FR-2.1/2.2 cover this, but do not specify that PROCESS_APPLICATION must remain changeable (editable) to any other type, not just pre-selected.

## Input 3 — Button Order and Style

Gaps:
- Input says "'Configure Now' should..." implying it already exists; PRD does not clarify whether "Configure Now" is new or pre-existing, which matters for scope.
- No gap in substance — FR-3.1 and FR-3.2 fully cover the input.

## Input 4 — Footer Version Display

Gaps:
- Input says "without '-SNAPSHOT'", but does not say SNAPSHOT should be stripped from all versions or only for display. FR-4.2 says "no -SNAPSHOT suffix in display" which is correct, but FR-4.3 ("injected at build time") is an implementation assumption not stated by the user — could also be runtime-resolved.

## Input 5 — Tag Chip Color Coding

Gaps:
- Input lists four categories explicitly; PRD renames "Standard" (input: "BPMN, DMN, CMMN, maybe with version") — the input treats standard/spec as a category separate from "BPMN Concepts"; PRD conflates BPMN Concepts and the standard version category under "Standard (with version e.g. BPMN 2.0)", losing the distinction.
- Input mentions "maybe with version BPMN 2.0, DMN 1.3, CMMN 1.1" as optional — FR-5.2 treats version suffixes as definite examples, potentially hardening an optional user preference.
- FR-5.4 (WCAG AA) and FR-5.5 (legend decision) are additions with no basis in the original input — added constraints not requested by the user.
