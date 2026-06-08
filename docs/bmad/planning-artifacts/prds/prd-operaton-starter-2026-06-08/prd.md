---
title: "UI Enhancements: Navigation, Tag Colors, Footer, and Project Configuration"
status: final
created: 2026-06-08
updated: 2026-06-08
project: operaton-starter
---

# PRD: UI Enhancements — Navigation, Tag Colors, Footer, and Project Configuration

## Overview

Focused UI and configuration improvements to the operaton-starter web application: clearer navigation and better defaults for first-time users, clean coordinates and configurable version for generated projects, and colored tag chips for gallery scannability.

**In scope:** `starter-web` frontend, `Metadata` API (tag type change), backend defaults for groupId/artifactId, Vite build pipeline (version injection).

---

## Features and Requirements

### F1 — Example Project Coordinates

**FR-1.1** The default `groupId` for generated projects SHALL be `org.operaton.example`.

**FR-1.2** The default `artifactId` and project name SHALL NOT contain the word "example". Naming follows the selected use case or project type (e.g. `my-process-app`, `leave-request`).

**FR-1.3** Both `groupId` and `artifactId` SHALL be freely editable. FR-1.1 and FR-1.2 govern defaults only.

**FR-1.4** The project generation form SHALL include a version field. The default value SHALL be `1.0.0-SNAPSHOT`. The field SHALL accept any value matching the Maven version format (non-empty, no whitespace); invalid input SHALL show an inline validation error and prevent generation.

---

### F2 — "Configure Now" Direct-to-Preview Flow

**FR-2.1** Clicking "Configure Now" on the main page hero SHALL navigate to `/configure` with `PROCESS_APPLICATION` pre-selected as the project type.

**FR-2.2** When `/configure` is reached without a `projectType` query parameter — via "Configure Now", direct URL entry, or a bookmark — the project type field SHALL be editable and the user may select any available project type.

**FR-2.3** When `/configure` is reached with a `projectType` query parameter (i.e. from a project type card or use-case card), the project type field SHALL be read-only, preserving existing behavior.

**FR-2.4** If `PROCESS_APPLICATION` is absent from the metadata response, the form SHALL fall back to the first available project type.

> **Rationale:** "Configure Now" is the primary CTA, hit before users explore project types. Making it editable removes a correction step for users who want a different type. The bookmark and direct-URL cases produce the same editable mode intentionally — it is correct behavior for any arrival without an explicit type.

---

### F3 — Main Page Navigation Button Order and Style

**FR-3.1** On the GalleryView hero section, the button order SHALL be:
  1. "Configure Now →" (primary CTA, unchanged style)
  2. "Project Types ↓"
  3. "Browse Use Cases ↓"

**FR-3.2** The "Project Types" button SHALL use the same visual style as "Browse Use Cases" (outlined: `border border-primary text-primary`).

---

### F4 — Footer Version Display

**FR-4.1** The footer SHALL display the operaton-starter version in the format `operaton-starter X.Y.Z`, alongside the existing Apache 2.0 and operaton.org links.

**FR-4.2** The displayed version SHALL NOT include a pre-release suffix (e.g. `-SNAPSHOT`, `-RC1`); such suffixes SHALL be stripped before display.

**FR-4.3** The version SHALL be injected at build time via the Vite environment variable `VITE_APP_VERSION`, sourced from the Maven POM (e.g. via Maven resource filtering or the Vite config). It SHALL work in both `vite dev` and `vite build` without manual update.

**FR-4.4** If `VITE_APP_VERSION` is absent or empty at build time, the footer SHALL display `operaton-starter` without a version string. It SHALL NOT display `undefined` or an empty token.

---

### F5 — Tag Chip Color Coding

**FR-5.1** Tag chips on `UseCaseGalleryCard`, `UseCaseCard`, and `ProjectTypeCard` SHALL be color-coded by tag category.

**FR-5.2** The following tag categories SHALL be supported:

| Category | Examples |
|---|---|
| `BPMN_CONCEPT` | User Task, Service Task, Timer, Message Event, Escalation, Compensation |
| `TECHNOLOGY` | Email, Queue, REST, Database, PDF |
| `PLATFORM` | Spring Boot, Quarkus, Tomcat, Postgres, App-Server |
| `STANDARD` | BPMN 2.0, DMN 1.3, CMMN 1.1 |

**FR-5.3** The tag data model SHALL change from `tags: string[]` to `tags: Tag[]` where `Tag = { label: string, category: TagCategory }`, applied to both `ProjectTypeInfo` and `UseCaseExample` in the API types and backend metadata. No backward-compatibility transition is required; the switch is direct (project is pre-production).

**FR-5.4** Tags with a missing or unrecognized `category` value SHALL render as a neutral grey chip.

**FR-5.5** The color palette per category is a UX implementation decision and SHALL meet WCAG AA contrast requirements for text on chip background.

**FR-5.6** Whether a legend explaining color-to-category mapping is displayed is a UX implementation decision.

---

## Non-Functional Requirements

**NFR-1** All UI changes SHALL remain consistent with the existing design system (Tailwind utility classes, `rounded-s`, `bg-primary`, neutral palette conventions).

**NFR-2** `VITE_APP_VERSION` injection SHALL require no manual step in the standard Maven build (`mvn package`, `mvn spring-boot:run`).

---

## Out of Scope

- Changes to generated project template content or structure
- New project types or use cases
- Accessibility audit beyond WCAG AA for tag chip colors
- Dark mode support for tag chip colors
- Validation of Maven coordinate fields beyond the version field (groupId, artifactId format)
