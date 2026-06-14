---
name: operaton-starter
status: final
updated: 2026-06-13
sources:
  - imports/ux-design-specification.md
  - imports/ux-color-themes.html
  - imports/ux-design-directions.html
  - ../../prds/prd-operaton-starter-examples-gallery-2026-06-13/prd.md
---

## 1. Foundation

| Dimension | Value |
|-----------|-------|
| Form factor | Web — single-page application |
| Framework | Vue 3 + Vue Router |
| UI system | Tailwind CSS v3 with Operaton design tokens (see DESIGN.md for all visual specs) |
| State management | Composables only — no Pinia, no Vuex |
| API client | OpenAPI-generated TypeScript fetch client (`src/generated/`) |
| Build tool | Vite — dev server proxies `/api` to `localhost:8080`; production build outputs to `starter-server/src/main/resources/static` |
| Type safety | TypeScript throughout |

Key composables:

| Composable | Responsibility |
|------------|---------------|
| `useMetadata()` | Fetches `MetadataResponse` (projectTypes, buildSystems, globalOptions); exposes `isLoading`, `error` |
| `useProjectForm(metadata)` | Reactive `formState: ProjectConfig`; per-field validation; computed `fileTree`; computed `shareableUrl`; `initFromQuery(route.query)` |
| `useGenerate(formState)` | `isGenerating`, `error`, `generate()` — triggers ZIP download |
| `useExamples()` | Reads `examples` slice from `MetadataResponse`; exposes `bySource`, `allTags`, `allIntegrations`, `runtimes`, `buildSystems`, `complexities` derived sets |
| `useGalleryFilters()` | Reactive `query` (free-text), `filters` (runtime / buildSystem / complexity / integrations sets); computed `filteredExamples`, `filteredUseCases`, `resultCount`; `clear()` |
| `useExampleDownload()` | Per-example `status: 'idle'\|'downloading'\|'success'\|'error'`, `error`, `download(example)` — drives the inline action-row states |

---

## 2. Information Architecture

### Surfaces

| Surface | Route | Reached from | Purpose |
|---------|-------|-------------|---------|
| Gallery | `/` | Direct URL, header nav | Persona-aware landing; browse project types; entry for Explorers and Practitioners |
| Configure | `/configure` | Gallery card "Configure →" CTA; hero "Configure Now →" CTA; shared URL | Full form + live interactive file tree preview + file content pane; generate & download |

**Project type on ConfigureView:** The project type is always pre-determined before reaching `/configure` (via gallery card, hero CTA, or shared URL `?projectType=` param). It is displayed as **read-only context** at the top of the form — not an editable field. If no `projectType` param is present, the default is shown as read-only context. Changing the project type requires returning to the gallery.

### Route architecture

```
/ (GalleryView)
  → card "Configure →" click
  → /configure?projectType=PROCESS_APPLICATION

/configure (ConfigureView)
  ← "← Back to gallery" ghost button
  → form state ↔ URL query params (bidirectional)
  → Generate & Download
  → IDE deep-link
  → Copy shareable link
```

**Gallery internal structure (post Examples Gallery update):** The Gallery surface holds, top to bottom: hero + "Configure Now →" CTA → **Project Types** subsection (unchanged primary entry — preserves the Marcus / Elena / Priya flows in §8) → **GallerySearchBar** (sticky, scoped to the sections below it) → **Examples** subsection → **Use Cases** subsection. The search input filters Examples + Use Cases by title/description/tags; Project Types are not affected by the search bar. The filter chip row (runtime / buildSystem / complexity / integrations) applies only to Examples. Each subsection has its own `<h2>` heading and a short blurb explaining what it contains.

### Not in MVP

- Dark mode (light mode only for V1)
- User accounts or saved configurations
- Multi-page wizard or stepped form
- In-browser ZIP preview
- In-UI repository management or manual refresh trigger (the FR-B9 refresh endpoint is API-only in v1)

---

## 2b. Inspiration & Anti-patterns

| Source | Behavioral patterns adopted | Why |
|--------|----------------------------|-----|
| `start.spring.io` | Single-page, no navigation; left-form / right-preview split; immediate visual feedback per change; generous whitespace; one accent color | Primary benchmark for Practitioner persona; sets the UX contract: no modals, no multi-step wizards, no popups |
| `code.quarkus.io` | Gallery grid of project-type cards; card anatomy (title + description + tags + persona hint); Explorer-first entry point | Reference for the gallery-first discovery path and card scale |
| `operaton.org` | Header/footer visual language; brand token source | Visual consistency with the Operaton ecosystem builds trust with both personas |

**Anti-patterns (from reference analysis):**

| Anti-pattern | Why avoided |
|---|---|
| Multi-step wizard form | Adds friction for Practitioners who need sub-30-second flow |
| Modal confirmation before download | Unnecessary interruption; file download is reversible |
| Spinner replacing card grid | Causes layout shift; skeleton cards preferred |
| Hover-only affordances | Inaccessible on touch and keyboard |
| Slow transitions (>200ms) on interactive elements | Tool context; users expect instant feedback |

---

## 3. Voice and Tone

The product speaks like a capable tool, not a marketing page. Short, directive, friendly.

### Microcopy

| Context | Do | Don't |
|---------|----|-------|
| Hero headline | "Start your Operaton project" | "Welcome to the Operaton Project Generator" |
| Primary CTA | "Configure Now →" | "Get Started Today" |
| Card CTA | "Configure →" | "Select This Option" |
| Download button | "Generate & Download" | "Submit" / "Create Project" |
| In-progress label | "Generating…" | "Loading…" / "Please wait" |
| Success message | "Downloaded {artifactId}.zip" | "Success! Your file is ready." |
| Back link | "← Back to gallery" | "Cancel" |
| Help icon label | "Help: {fieldLabel}" (aria-label) | "Info" |

### Field labels

Labels are sentence-case, above the input, never floating. Every field label includes a `[?]` help icon that reveals inline help on click.

| Field | Label | Type | Example / default |
|-------|-------|------|-------------------|
| projectType | "Project Type" | read-only badge | `Process Application` (contextual, not editable) |
| groupId | "Group ID" | text input | `com.example` |
| artifactId | "Artifact ID" | text input | `my-process-app` |
| projectName | "Project Name" | text input | `My Process App` |
| buildSystem | "Build System" | radio (Maven / Gradle) | `Maven` |
| buildSystem.gradleDsl | "Gradle DSL" | radio sub-option (Groovy / Kotlin) | revealed only when Gradle selected; no default pre-selected |
| deploymentTarget | "Target Runtime" | select | revealed only for Process Archive; MVP options: Tomcat, Wildfly |
| dependencyUpdates | "Dependency Updates" | checkbox (opt-in, off by default) | unchecked |
| dependencyUpdates.flavour | "Dependency Updater" | radio sub-option (Dependabot / Renovate) | revealed only when checkbox checked |
| dockerCompose | "Docker Compose" | checkbox (opt-in, off by default); hidden for Process Archive | unchecked |
| githubActions | "GitHub Actions" | checkbox (opt-in, off by default) | unchecked |

**Conditional visibility rules:**
- `buildSystem.gradleDsl` sub-option: shown only when Gradle is selected; hidden (not disabled) when Maven is selected.
- `deploymentTarget`: shown only for Process Archive project type; hidden for Process Application.
- `dependencyUpdates.flavour` sub-option: shown only when the Dependency Updates checkbox is checked; hidden when unchecked.
- `dockerCompose`: shown only for project types that support containerised embedded deployment (Process Application); hidden for Process Archive.
- All Extras options (Dependency Updates, Docker Compose, GitHub Actions) default to off; developer must explicitly enable each.

### Inline help pattern

Every field has a `<HelpIcon>` component: a small circular `?` button adjacent to the label. Click toggles a `<div>` (no role) below the field with a short explanation. The accordion uses `aria-expanded` on the button. Help text is never surfaced proactively — it is always on demand.

### Error message conventions

| Error type | Surface | Copy pattern |
|------------|---------|-------------|
| Validation (field) | Below the input, `aria-describedby` on the field | "Use lowercase letters, numbers, and dots only" (groupId); "Use lowercase letters, numbers, and hyphens only" (artifactId); "Project name is required" (projectName) |
| API / network failure | `<ErrorBanner>` at view top, `role="alert"` `aria-live="assertive"` | `{error.title}` (medium) + `{error.detail}` (small) — from ProblemDetail |

---

## 4. Component Patterns

All visual properties (colors, radii, spacing, shadows) are in DESIGN.md. This section covers behavior only.

### `<ProjectTypeCard>`

- Rendered as `<li>` inside a `<ul role="list">` card grid. The card itself is a non-interactive container; the **"Configure →" `<a>` element** is the sole interactive and focusable element per card.
- Anatomy: heading (`{typography.heading-3}`), description, tag list (see `{components.tag}`), persona hint, expandable help `<HelpIcon>`, "Configure →" `<a href="/configure?projectType={id}">` (`{components.button-primary}`).
- **"Configure →" link** is keyboard-accessible via `Tab` + `Enter`; it carries the card's accessible name via `aria-label="{displayName} — Configure"`.
- **`<HelpIcon>` button** (`aria-expanded`) → toggles an inline accordion below the persona hint. Accordion transition: `max-height` 200ms ease-out. `Escape` closes.
- Hover on card: border transitions to `{colors.primary}` + shadow elevates (see `{components.card}`); transition 150ms. Triggered on `<li>` card container via CSS `:hover`.
- No `tabindex` on the `<li>` container — only the `<a>` and `<HelpIcon>` button are focusable within the card.

### `<FileTreePreview>`

- Rendered as `<section aria-label="File structure preview" aria-live="polite">`.
- File tree is a **pure computed property** of `formState` + `metadata.projectTypes[selectedType].templateManifest`.
- Condition evaluation: simple equality parser (`field == 'value'`) — no `eval()`, no server round-trip.
- Identity interpolation: `artifactId`, `groupId`, `projectName` are substituted into file paths where the manifest uses them.
- Preview updates are **instant** (no debounce, no animation — the `aria-live="polite"` region announces changes to screen readers naturally).
- File tree rendered as a plain `<ul>`/`<li>` nested list with `aria-label="Project file structure"` on the root `<ul>`. Styled via `{components.file-tree-preview}`. Monospace font (`{typography.code}`).
- **Each file item is interactive** — `<button>` element (not anchor) that triggers file content display in the adjacent `<FileContentPane>`. The currently-selected item receives `aria-selected="true"` and the selected visual treatment (`{components.file-tree-preview.item-selected-*}`). Keyboard: `Tab` focuses the first item; `Arrow Up`/`Arrow Down` navigate; `Enter` or `Space` selects.
- `aria-live="polite"` is on the `<section>` wrapper, but the announcement is debounced: a separate `role="status"` element announces a summary ("File tree updated") 600ms after the last change. The visual tree updates instantly; only the status announcement is debounced, preventing screen reader verbosity on each keystroke.
- When the file tree updates due to form state changes, the selected item is retained if it still exists in the new tree; otherwise selection clears and the `<FileContentPane>` shows an empty/placeholder state.

### `<FileContentPane>`

- Adjacent to `<FileTreePreview>` in the right (preview) panel, taking up the remaining height above `<ActionPanel>`.
- Rendered as `<section aria-label="File content preview">`.
- Shows the filename as a small label (`{components.file-content-pane.filename-foreground}`) above a scrollable monospace content area.
- Content is `TemplateManifestEntry.previewContent` — **static representative template source**, not dynamically generated from current form state. A note (visually subtle, e.g. below the pane) states: "Preview shows template structure — actual content reflects your configuration after download."
- When no file is selected (initial state): shows a placeholder prompt "← Select a file to preview its content" centred in the pane.
- No generation or server round-trip is required to show content.
- `role="region"` with `aria-live="polite"` — announces filename when selection changes.

### `<ConditionalSubOption>` (behavioral pattern)

Applies to: `buildSystem.gradleDsl`, `deploymentTarget`, `dependencyUpdates.flavour`.

- The sub-option container is hidden (`display: none` / `v-if`) when the parent condition is false; rendered and visible when true.
- Reveal animation: `max-height` 200ms ease-out — same as help accordion. `prefers-reduced-motion` suppresses animation.
- Visual treatment: `{components.conditional-sub-option}` — left-border indent communicates hierarchy.
- When a sub-option is hidden (parent unchecked / wrong type selected), its value is not included in `formState` for URL serialisation or generation — the field is absent, not empty.
- Screen reader: the parent control's label does not reference the sub-option; the sub-option has its own `<label>` that is announced when it becomes focusable.

### `<HelpIcon>`

- Small circular `?` button adjacent to each label. Visual spec: `{components.help-icon}` (16×16px visible; 44×44px touch target via padding — WCAG 2.5.5).
- On click: toggles `expanded` boolean; renders `<div>` (no extra role) below the field. The `aria-expanded` + `aria-controls` pattern on the button is sufficient — no `role="note"` needed on the content div.
- `Escape` closes open help panel; focus returns to the `<HelpIcon>` button.

### `<ErrorBanner>`

- Visible only when `error` prop is non-null.
- Full-width within its parent view (not a toast or overlay).
- Styled per `{components.error-banner}`.
- `role="alert"` + `aria-live="assertive"` — announced immediately by screen readers.
- Structure: warning icon + `error.title` (medium weight) + `error.detail` (small).
- Placed at the top of GalleryView and ConfigureView respectively.

### `<SkeletonCard>`

- Displayed in gallery while `useMetadata().isLoading === true`.
- Matches `<ProjectTypeCard>` dimensions exactly — prevents layout shift.
- `{components.skeleton}` fill with CSS `animate-pulse`.
- Container carries `aria-busy="true"` while loading.

### `<ActionPanel>` (within ConfigureView)

- Located in the right (preview) panel, below `<FileContentPane>` and `<FileTreePreview>`.
- Right panel layout (top to bottom): `<FileTreePreview>` + `<FileContentPane>` (side-by-side or stacked depending on available height) → `<ActionPanel>`.
- Contains three sub-sections:
  1. **IDE Deep Links**: "Open in IntelliJ IDEA" (`{components.button-secondary}`) + "Open in VS Code" (`{components.button-secondary}`).
  2. **Shareable URL**: "Copy Link" (`{components.button-secondary}`) — copies the computed `shareableUrl` to clipboard.
  3. **Generate & Download**: `{components.button-primary}` — calls `useGenerate().generate()`; while `isGenerating` shows spinner + "Generating…" text; button disabled during generation.

---

## 5. State Patterns

| State | GalleryView | ConfigureView |
|-------|-------------|---------------|
| **Loading** (metadata fetch) | Skeleton cards in grid; `aria-busy="true"` on container | Form shimmer; preview panel empty |
| **Generating** (ZIP in progress) | — | "Generate & Download" button disabled; spinner + "Generating…" label |
| **Error** (API / network) | `<ErrorBanner>` at top; skeleton replaced with error state | `<ErrorBanner>` at top; form remains interactive |
| **Success** (download complete) | — | Transient success message "Downloaded {artifactId}.zip" near action panel |
| **Empty** (no project types from API) | `<ErrorBanner>` — treated as an API error state | — |
| **First-visit** | Gallery loads with defaults; hero section visible | Form pre-filled with defaults; project type shown as read-only context from query param or default |
| **Invalid query param** (e.g., `?projectType=NONEXISTENT`) | — | Form silently falls back to defaults; no error shown (silent ignore is intentional — unknown params are discarded by `initFromQuery`) |
| **Empty project types** (API 200 but empty array) | `<ErrorBanner>` — treated same as API error state; no empty-gallery UI | — |
| **File selected** (file tree item clicked) | — | `<FileContentPane>` shows `TemplateManifestEntry.previewContent` for the selected file |
| **File tree updated** (form change invalidates selection) | — | If selected file still exists: content pane retains it; if removed from tree: pane resets to placeholder |
| **Conditional sub-option revealed** (e.g., Gradle selected) | — | Sub-option slides into view; parent form layout reflows smoothly |

---

## 6. Interaction Primitives

### Keyboard map

| Key | Action |
|-----|--------|
| `Tab` | Next focusable element — in Configure two-column layout: form fields first (left), then file tree items (right), then action panel buttons (right); top-to-bottom within each column |
| `Shift+Tab` | Previous focusable element (reverse of Tab order above) |
| `Enter` | Activate focused button, card, or file tree item (show content) |
| `Space` | Toggle checkbox / activate button / select file tree item |
| `Arrow Up` / `Arrow Down` | Navigate within radio button groups **or** within file tree items when tree has focus |
| `Escape` | Close open accordion or help panel |

### Form interaction rules

- All changes to form fields update `formState` reactively; `shareableUrl` and `fileTree` recompute immediately.
- URL query params are updated to match `formState` (browser history updated via `router.replace`, not `router.push` — no extra history entries per keystroke).
- On page load, `useProjectForm.initFromQuery(route.query)` reads all params; valid params override defaults; unknown or invalid params are silently ignored.
- Validation runs on blur for text fields; invalid state shown below the field via `aria-describedby` linking the input to its error element (e.g., `<input aria-describedby="groupId-error">` + `<span id="groupId-error">`). Field-level errors do not use `role="alert"` — that is reserved for the global `<ErrorBanner>` only.

### Transition timing

| Transition | Duration | Easing |
|------------|----------|--------|
| Route change (fade) | 150ms | linear |
| Card hover (border + shadow) | 150ms | ease |
| Help accordion expand | 200ms | ease-out |
| Preview update | instant | — |
| Button loading state | instant | — |

### Shareable URL behavior

Query parameters are human-readable and mirror OpenAPI field names:

```
/configure?projectType=PROCESS_APPLICATION&buildSystem=GRADLE_KOTLIN&groupId=org.myco&artifactId=my-app
```

"Copy Link" copies this URL to the clipboard. Recipients opening the URL get the form pre-filled with those values.

### IDE deep-link protocol

| IDE | Protocol | Mechanism |
|-----|----------|-----------|
| IntelliJ IDEA | `idea://com.intellij.ide.starter?url={encoded_generate_url}` | IDEA fetches ZIP via Spring Initializr protocol |
| VS Code | `vscode://vscjava.vscode-spring-initializr/open?url={encoded_generate_url}` | Extension-dependent; fallback is documented download workflow |

`{encoded_generate_url}` is the full `/api/v1/generate` URL with form fields encoded as query params.

---

## 7. Accessibility Floor

Standard: **WCAG 2.1 AA**.

### ARIA patterns

| Component | Pattern |
|-----------|---------|
| Gallery card grid | `<ul role="list">` container; each card is `<li>` (no interactive role on container) |
| Gallery card CTA | `<a href="/configure?projectType={id}" aria-label="{displayName} — Configure">` — sole focusable element per card |
| File tree | `<ul aria-label="Project file structure">` nested list; each file item is a `<button>` with `aria-selected="{bool}"` on its `<li>` wrapper |
| Live preview wrapper | `<section aria-live="polite">` + debounced `<div role="status">` for keystroke announcements |
| File content pane | `<section aria-label="File content preview" aria-live="polite">` — announces filename when selection changes |
| Error banner | `role="alert"`, `aria-live="assertive"` |
| Loading container | `aria-busy="true"` |
| Help accordion button | `aria-expanded="{bool}"`, `aria-controls="{contentId}"` |
| Help panel content | Plain `<div>` — no role (accordion button handles announcement) |
| Radio groups | `<fieldset>` + `<legend>` |
| Conditional sub-options | Parent control does not reference sub-option in its label; sub-option has its own `<label>`; container uses `v-if` (not `display:none`/`aria-hidden`) — removed from DOM when hidden |
| Project type read-only display | `<span>` or `<div>` with visible label; not a form control; no `aria-*` form attributes needed |
| Validation errors (field) | `aria-describedby` linking `<input>` to error `<span id="{field}-error">` — announced when field is focused |
| API / network errors | `role="alert"` + `aria-live="assertive"` on `<ErrorBanner>` only |

### Skip navigation

```html
<a href="#main-content"
   class="sr-only focus:not-sr-only focus:absolute focus:top-4 focus:left-4
          bg-primary text-white px-4 py-2 rounded-s z-50">
  Skip to main content
</a>
```

Rendered in `<AppHeader>` as the first focusable element in the document.

### Focus management rules

- Focus ring applied globally via `*:focus-visible`: `{components.focus-ring}`.
- `outline: none` is never applied without a custom visible alternative.
- Tab order matches the visual layout (left-to-right, top-to-bottom).
- On route navigation, focus moves to `#main-content`. Implementation: `router.afterEach(() => nextTick(() => document.getElementById('main-content')?.focus()))`. The `#main-content` element must have `tabindex="-1"` so it can receive programmatic focus without appearing in the tab order.

### Motion and animation

All transitions and `animate-pulse` skeleton animations must be suppressed when `prefers-reduced-motion: reduce` is active. Use Tailwind's `motion-reduce:` variant on all transition and animation classes.

### Text resize

Layouts must remain functional at 200% text zoom (distinct from browser page zoom). Fixed `px` heights are replaced with `rem`-based `min-height`. The header uses `min-height: 4rem`, not a fixed `64px`.

### CI enforcement

The `lint-web` GitHub Actions job runs `npm run test:a11y` (vitest + `vitest-axe`), which runs axe-core against rendered `GalleryView` and `ConfigureView`. Zero violations = pass; any violation = build fails.

---

## 8. Key Flows

### Flow 1 — Marcus (Practitioner / Returning Developer)

Marcus knows Operaton and knows what he wants. His benchmark is `start.spring.io`.

1. Arrives at `/`.
2. Sees hero section; clicks **"Configure Now →"** — bypasses gallery entirely.
3. Navigated to `/configure` with default project type pre-selected.
4. Fills in `groupId`, `artifactId`, `projectName`.
5. Keeps all other defaults (Maven, Java 17, Renovate, GitHub Actions on).
6. Watches file tree update as he types — confirms he's getting what he expects.
7. Clicks **"Generate & Download"**.
8. ZIP downloads as `{artifactId}.zip`.
9. **Climax**: Total time under 30 seconds. No surprises, no modals.

**Failure path**: If `generate()` fails (network error, server error), `<ErrorBanner>` appears at the top of ConfigureView. The form remains fully interactive; Marcus can retry without re-filling any field.

### Flow 2 — Elena (Camunda 7 Migrator / Explorer)

Elena is BPMN-literate but new to Operaton. She needs discovery and confidence.

1. Arrives at `/`; sees gallery cards.
2. Hovers over "Process Application" card; notices hover state.
3. Clicks **"[?] More about this project type"** — inline accordion expands.
4. Reads persona hint: "Ideal for Camunda 7 migrators…"
5. Clicks **"Configure →"** on the card.
6. Navigated to `/configure?projectType=PROCESS_APPLICATION` — project type is pre-selected.
7. Uses `[?]` help icons on individual fields to understand each option.
8. Configures and downloads.
9. **Climax**: Elena understands what she's getting before she clicks Configure. No docs tab opened.

**Failure path**: If metadata fails to load, `<ErrorBanner>` replaces the skeleton cards in GalleryView. Elena can refresh; her journey has not yet begun.

### Flow 3 — Thomas (BPMN Newcomer / Team Collaboration)

Thomas wants to share a configuration with a teammate.

1. Thomas configures form at `/configure`.
2. Notices "Copy Link" button in the action panel.
3. Clicks → current `shareableUrl` copied to clipboard; URL query params already reflect his configuration.
4. Pastes URL in Slack, sends to teammate.
5. Teammate opens URL; form pre-fills from query params.
6. Teammate adjusts `groupId` for their namespace, then downloads.
7. **Climax**: Zero configuration file exchange. One URL carries the full intent.

**Failure path**: If the clipboard API is unavailable (non-HTTPS, blocked permission), "Copy Link" degrades to a read-only URL `<input>` pre-selected for manual copy.

### Flow 4 — Priya (Platform Engineer / IDE Integration)

Priya wants to open the project directly in IntelliJ IDEA without touching the filesystem manually.

1. Priya configures form at `/configure`.
2. Sees **"Open in IntelliJ IDEA"** button in the action panel.
3. Clicks button.
4. Browser fires the `idea://` deep-link; IDEA opens (if installed), fetches ZIP, imports project.
5. No download folder interaction, no manual import step.
6. **Climax**: The generated project opens ready to run — no friction between browser and IDE.

**Failure path**: If IntelliJ IDEA is not installed, the browser shows a protocol error. The standard **"Generate & Download"** button is always visible as fallback — Priya downloads the ZIP and imports manually.

### Flow 5 — Keyboard-Only User

A developer who navigates entirely without a mouse.

1. Skip-nav link is the first tab stop → user can jump to `#main-content`.
2. `Tab` into gallery card grid; cards receive focus with visible `{components.focus-ring}`.
3. `Enter` on a card → navigates to `/configure`.
4. `Tab` through form fields in visual order.
5. `Arrow keys` navigate radio groups (Project Type, Build System, Java Version).
6. `Space` toggles checkboxes (GitHub Actions, Docker Compose).
7. `Tab` to **"Generate & Download"**; `Enter` triggers download.
8. **Climax**: Every step reachable and operable with keyboard alone. No mouse required at any point.

---

## 9. Responsive & Platform

### Breakpoints

| Breakpoint | Viewport | Layout change |
|------------|----------|---------------|
| Mobile | < 768px | Single column — form above, preview below (collapsible `<details>`) |
| Tablet | ≥ 768px | Two-column layout: form left (`min-w: 420px`), preview right (`flex: 1`) |
| Desktop | ≥ 1024px | Two-column with wider preview panel |
| Wide | ≥ 1280px | Both columns capped at `{spacing.content-max-width}` (80rem), centered |

### Mobile adaptations

| Element | Mobile behavior |
|---------|----------------|
| Gallery grid | `grid-cols-1` — full-width cards |
| File tree preview | Collapses to `<details>` disclosure below form |
| Header navigation | Hamburger menu (if nav items added beyond MVP) |
| All buttons | Minimum height 44px — WCAG 2.5.5 touch target size |
| `<HelpIcon>` | 44×44px touch target (via padding); visible icon remains 16×16px |
| Checkboxes and radio buttons | Minimum 44px touch target area |
| Form fields | Full-width stacked layout |

### Platform notes

- Light mode only — dark mode is explicitly out of scope for MVP.
- Browser support: two most recent major versions of Chrome, Firefox, and Safari (Tailwind + Vue 3 baseline).

---

## 10. Examples Gallery (added 2026-06-13)

Extension to the Gallery surface. PRD: `../../prds/prd-operaton-starter-examples-gallery-2026-06-13/prd.md`. Inherits all prior IA, voice, components, states, and a11y rules unless explicitly overridden below. DESIGN.md owns visual specs.

### 10.1 Microcopy

| Context | Do | Don't |
|---------|----|-------|
| Subsection heading | "Examples" / "Use Cases" / "Project Types" | "Sample projects" / "Templates" |
| Examples blurb | "Runnable example projects published by the Operaton community. Download a ZIP and open in your IDE." | "Browse our amazing example library" |
| Search placeholder | "Search examples and use cases…" | "Type to search" |
| Filter chip aria-label | "Filter by {label}" (e.g., "Filter by Spring Boot") | "Toggle Spring Boot" |
| Active chip aria-label | "Remove filter: {label}" | "Disable Spring Boot" |
| "Clear filters" ghost button | "Clear filters" | "Reset" |
| Result counter (live region) | "{n} examples, {m} use cases match" / "1 example matches" / "No matches" | "Showing results" |
| Download primary CTA | "Download ZIP" | "Get this example" / "Download" |
| Download in-progress | "Downloading…" | "Loading…" / "Please wait" |
| Download success (transient) | "Downloaded {exampleId}.zip ✓" | "Success!" |
| Download failure inline | "Couldn't download. {short reason}. **Retry**" | "Error occurred" |
| View on GitHub | "View on GitHub ↗" | "See repo" / "Source" |
| Details disclosure | "More details ↓" / "Hide details ↑" | "Show more" |
| Empty: no examples loaded | "No examples are available right now. Maintainers can register example repositories — see the format docs." + "View format docs →" | "Coming soon" |
| Empty: no filter matches | "No examples match these filters." + "Clear filters" | "0 results" |
| Card SHA footer | "@ {short-sha}" using mono font, no label | "Commit: {sha}" |

### 10.2 Component Patterns

All visual specs in DESIGN.md.

#### `<GallerySearchBar>`

- Rendered as `<div role="search">` containing `<input type="search" aria-label="Search examples and use cases">` + a `<div role="toolbar" aria-label="Filter examples">` of `<FilterChip>`s.
- Sticky position via CSS `position: sticky; top: {spacing.header-height}`. Initial render has no shadow; on `IntersectionObserver` triggering past its anchor, applies `{components.search-bar.sticky-shadow}`.
- Search input is **debounced 200ms** before updating the shared filter state — prevents thrashing the result counter and live region.
- A visually subtle live region `<span role="status" aria-live="polite" class="sr-only">` announces the result count after each debounced change.
- "Clear filters" appears (as `{components.button-ghost}`) only when at least one chip is active or the query is non-empty.

#### `<FilterChip>`

- `<button type="button" aria-pressed="{active}">` — the `switch` role pairs cleanly with each chip representing an independent on/off filter value.
- Keyboard: `Tab` focuses, `Space`/`Enter` toggles. Inside the toolbar, `Arrow Left`/`Arrow Right` move focus between chips (standard toolbar semantics).
- Active chips visually carry an "×" glyph on hover/focus (informational only — the toggle is still `Space`/`Enter`).
- Visible focus ring via the global `*:focus-visible` rule.

#### `<ExampleGalleryCard>`

- Rendered as `<li>` inside `<ul role="list">` (the Examples subsection grid). Like `<ProjectTypeCard>`, the `<li>` is a non-interactive container.
- **Focusable elements within a card**: (1) "View on GitHub" link, (2) "Download ZIP" button, (3) "More details" disclosure button. The whole card is **not** a single clickable target — examples carry too much metadata to collapse onto one action.
- Anatomy per DESIGN.md `{components.example-card}`. Icon slot renders: (a) the emoji character at 1.5rem if `icon` is a single grapheme, (b) the fetched image otherwise, (c) a default neutral SVG glyph if `icon` is absent or the image load fails.
- Hover: card border transitions to `{colors.primary}` + shadow elevates (inherits `{components.card}`).
- Details disclosure uses `aria-expanded` + `aria-controls`; the panel itself is a `<div>` (no role). `Escape` collapses an open panel and returns focus to the disclosure button.
- The pinned SHA footer renders the short SHA inside the details panel; the GitHub link's `href` includes the SHA so the link survives later force-pushes.

#### `<ExamplesEmptyState>`

- Rendered as `<div role="region" aria-label="Examples">`'s child when the filtered list is empty.
- Two messages selected by cause (no examples loaded vs. filter mismatch); see microcopy table.
- Single CTA button styled per `{components.button-ghost}`.

#### `<DownloadAction>` (sub-pattern inside `<ExampleGalleryCard>`)

A small state machine driven by `useExampleDownload()`. Renders exactly one of: idle button, disabled+spinner button, transient success label, or inline error block. Transitions are **not animated** beyond a 100ms fade — fast feedback matters more than polish here.

### 10.3 State Patterns (additions)

| State | Surface | Behavior |
|-------|---------|----------|
| **Examples loading** | Gallery — Examples subsection | Skeleton cards matching `<ExampleGalleryCard>` dimensions; `aria-busy="true"` on subsection container; metadata badges and tag rows rendered as `{components.skeleton}` blocks |
| **All sources failed** | Gallery — Examples subsection | `<ExamplesEmptyState>` with the "no examples loaded" copy; **no** global `<ErrorBanner>` (the rest of the gallery still works) |
| **Some sources failed** | Gallery — Examples subsection | Subsection renders the examples that did load; failed sources are silent in the UI (logs only, per PRD FR-B7) |
| **No filter matches** | Examples + Use Cases subsections | `<ExamplesEmptyState>` with the "no matches" copy + "Clear filters" button |
| **Download in progress** | Card action row | Button disabled, label "Downloading…", spinner glyph; other cards remain interactive |
| **Download success** | Card action row | Transient `{components.download-success-inline}` for 3s, then idle |
| **Download failed** | Card action row | `{components.card-error-inline}` rendered below action row with retry; never escalates to global `<ErrorBanner>` |
| **Details expanded** | Card | `aria-expanded="true"` on the disclosure; details panel rendered; reflows the gallery grid via natural document flow (no absolute positioning) |
| **Icon image load failed** | Card | Silent fallback to the neutral SVG glyph; no error surfaced to the user |

### 10.4 Interaction Primitives (additions)

| Action | Key(s) / behavior |
|--------|------------------|
| Search input | Type → 200ms debounce → filter applies + live-region announces count |
| Toggle filter chip | `Space` / `Enter` while chip focused |
| Move within filter chip toolbar | `Arrow Left` / `Arrow Right` (toolbar semantics — overrides global `Tab` flow within the toolbar group) |
| Open card details | `Enter` / `Space` on the disclosure button |
| Close card details | `Escape` while focus inside the card's details panel — returns focus to disclosure |
| Download | `Enter` / `Space` on "Download ZIP" button |
| Retry after failure | `Enter` / `Space` on inline "Retry" affordance |

**Filter and search semantics:**

- Search query matches against `title`, `shortDescription`, `tags[].label`, `integrations[]` (case-insensitive substring).
- Active filter chips combine with **AND across filter categories** and **OR within a category** (e.g., `runtime=spring-boot` AND (`integration=kafka` OR `integration=rest`)).
- Filters and search compose: search narrows further within the filter-matched set.
- Filter chip state is **not** mirrored to URL query params in v1 — keeps the existing `?projectType=` semantics clean. `[ASSUMPTION]` deep-linking filtered views is not yet a needed user journey.

### 10.5 Accessibility (additions)

| Pattern | Implementation |
|---------|---------------|
| Search bar | `<div role="search">` wrapper; `<input type="search" aria-label="Search examples and use cases">` |
| Filter chip group | `<div role="toolbar" aria-label="Filter examples">` containing `<button type="button" aria-pressed="{bool}">` per chip |
| Result counter | `<span role="status" aria-live="polite" class="sr-only">` updated after debounce |
| Example card | `<li>` inside `<ul role="list">`; non-interactive container |
| Card details disclosure | `aria-expanded` + `aria-controls` on the toggle button; panel itself is plain `<div>` |
| Card-local error | Plain `<div>` (not `role="alert"`) — failure is scoped and the user just triggered the action; no need to interrupt SR users globally. Includes a focusable `Retry` button |
| Icon slot | Emoji renders inside a `<span aria-hidden="true">`; the card's accessible name comes from its title. Image-based icons use `alt=""` (decorative) |
| Sticky search bar | Remains in the document flow (does not trap focus); `Tab` exits naturally into the gallery content below |
| Live SHA footer | Visual-only; not announced by screen readers (wrapped in `aria-hidden="true"` since it adds no journey value for SR users) |

WCAG 2.5.5 target size: filter chips minimum 32px height with the chip container providing a 44×44px touch target via padding on mobile.

### 10.6 Key Flow — Anna (Examples Browser)

Anna is a backend developer evaluating Operaton. She knows she wants a Spring Boot starter with Kafka.

1. Lands on `/` from a search engine.
2. Sees the sticky **GallerySearchBar** under the hero — types `kafka`.
3. Live region announces "1 example, 0 use cases match"; the Examples subsection re-renders with the Order Fulfillment card.
4. Notices the card icon (📦), reads the shortDescription ("End-to-end order fulfillment process on Quarkus with Kafka events…").
5. Realizes she wanted Spring Boot, not Quarkus. Clears the search and clicks the "Spring Boot" filter chip in the sticky bar.
6. Examples subsection shows the Leave Request card. She clicks **"More details ↓"** — reads longDescription, sees the integrations list, the SHA at the bottom.
7. Clicks **"Download ZIP"**. Button enters disabled+spinner state for ~2s. Replaced by "Downloaded leave-request-spring-boot.zip ✓" for 3s.
8. **Climax**: Anna had a runnable starter on disk less than 30s after landing — and could see the build system, runtime, and prerequisites *before* downloading.

**Failure path 1** — repository unreachable at startup: the Examples subsection renders `<ExamplesEmptyState>` ("No examples are available right now…"); Use Cases section still works; Anna can fall back to the existing flow.

**Failure path 2** — download fails: card-local `{components.card-error-inline}` appears below the action row with a Retry button. The rest of the gallery stays fully interactive.

### 10.7 Responsive Notes (additions)

| Breakpoint | Behavior |
|------------|----------|
| Mobile (< 768px) | GallerySearchBar wraps: search input on row 1, filter chips on row 2 (horizontally scrollable rather than wrapping further). Example cards full-width. Sticky behavior preserved. |
| Tablet (≥ 768px) | Search bar single row; chip row wraps to a second line when chip count exceeds row width. Cards: 2 columns. |
| Desktop (≥ 1024px) | Cards: 3 columns. |
| Wide (≥ 1280px) | Cards remain 3 columns within `{spacing.content-max-width}`. |
