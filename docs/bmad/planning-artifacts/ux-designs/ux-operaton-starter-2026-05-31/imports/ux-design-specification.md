---
stepsCompleted: ['step-01-init', 'step-02-discovery', 'step-03-core-experience', 'step-04-emotional-response', 'step-05-inspiration', 'step-06-design-system', 'step-07-defining-experience', 'step-08-visual-foundation', 'step-09-design-directions', 'step-10-user-journeys', 'step-11-component-strategy', 'step-12-ux-patterns', 'step-13-responsive-accessibility', 'step-14-complete']
workflowStatus: complete
completedAt: '2026-03-28'
inputDocuments:
  - 'docs/bmad/planning-artifacts/prd.md'
  - 'docs/bmad/planning-artifacts/epics.md'
  - 'docs/bmad/planning-artifacts/architecture.md'
workflowType: 'ux-design'
---

# UX Design Specification — operaton-starter

**Author:** Karsten
**Date:** 2026-03-28

---

## 1. Project Understanding

### Product Summary

Operaton Starter is a stateless, open-source project generator at `start.operaton.org` that bootstraps Operaton-based projects as downloadable, immediately runnable project archives. It fills the same gap that Spring Initializr fills for Spring Boot.

### Target Users

**Practitioners** — Operaton-familiar developers who know what they want. They arrive with a goal: pick a project type, tweak identity fields, download. Friction is the enemy. They benchmark this against `start.spring.io` and tolerate no cognitive overhead.

**Explorers** — BPMN-literate developers new to Operaton, including Camunda 7 migrators. They need discovery, context, and confidence before committing. They need to understand what project types exist and what each one does.

> "Today's Explorer is tomorrow's Practitioner." The quality of the first session determines ecosystem retention.

### What Makes This Special

- **First dedicated Operaton project initializer** — no alternative exists.
- **Split landing page** — Practitioner path (direct form) and Explorer path (visual gallery) serve both personas without compromise.
- **Live file tree preview** — client-side, zero server round-trips per change.
- **MCP module** — natively callable from AI assistants.
- **Benchmark competition**: `start.spring.io` (Practitioner UX reference) and `code.quarkus.io` (Explorer/gallery UX reference).

---

## 2. Core Experience Definition

### Experience Pillars

**1. Zero friction for Practitioners**
A Practitioner arrives, configures, downloads in under 30 seconds. No modals, no sign-up, no unnecessary decisions. The form defaults are correct for 80% of users on first visit.

**2. Guided discovery for Explorers**
The gallery is informative, not overwhelming. Cards explain what each project type does, who it is for, and what it includes — without requiring the user to navigate away.

**3. Confidence through transparency**
The live file tree preview removes the "what will I get?" question entirely. The user sees the exact output before downloading. No surprises.

**4. Professional quality**
The experience benchmarks against `start.spring.io`. It looks like it belongs in the Operaton ecosystem. It does not feel like a side project.

**5. Inclusive by default**
Keyboard-complete. WCAG 2.1 AA. Every developer, regardless of input method, gets the same experience.

### Core Interaction Loop

```
Landing (/) → Gallery or Direct Form → Configure → Preview Updates Live → Generate & Download
```

The gallery card click pre-selects the project type in the form, collapsing two steps into one for Explorers.

---

## 3. Emotional Response Goals

| Moment | Target Emotion |
|--------|---------------|
| First landing | "This looks professional — part of the Operaton family" |
| Gallery browsing | "I understand what these project types are for" |
| Form configuration | "This is fast — sensible defaults, clear options" |
| Live preview | "I see exactly what I'm getting" |
| Download click | "That was genuinely easy" |
| First `mvn verify` passing | "This project just works" |

**Anti-patterns to avoid:**
- Cognitive overload from too many options at once
- Surprise at the zip contents ("why is this file here?")
- Frustration from slow feedback loops
- Feeling lost without mouse access

---

## 4. UX Pattern Analysis & Inspiration

### Primary Inspiration: start.spring.io

**What to adopt:**
- Single-page, no navigation — everything in one cohesive view
- Left panel: configuration options with clear groupings
- Right panel: metadata (preview equivalent)
- Clean, generous whitespace
- Immediate visual feedback on option changes
- Simple, uncluttered color use — mostly neutral with one accent

**What to improve upon:**
- File tree preview is richer than Spring Initializr's package listing
- Gallery entry point that Spring doesn't have

### Secondary Inspiration: code.quarkus.io

**What to adopt:**
- Extension/project type gallery as visual cards
- Card design: title + description + tags + persona hint
- Grid layout for card browsing
- Search/filter patterns for the gallery

**What to improve upon:**
- Operaton Starter's gallery is simpler (2 project types MVP) — cards can be larger and more informative

### Tertiary Inspiration: Operaton.org

**What to adopt:**
- Exact color palette: `--color-primary: #184AEF`, `--color-secondary: #27F3E0`
- Typography: matches the Operaton brand
- Header/footer visual language
- Border radius and spacing patterns from the design system

---

## 5. Design System

### Approach: Tailwind CSS with Operaton Design Tokens

**Decision**: Tailwind CSS v3 with custom theme extending the operaton.org design tokens.

**Rationale:**
- Vue 3 stack is already configured with Tailwind (Story 4.1 requirement)
- Custom theme maps operaton.org tokens → Tailwind utility classes
- Complete visual uniqueness while using proven utility-first patterns
- JIT purge requires all Tailwind classes to be static strings (per Story 4.1 AC)

### Design Token Extraction from operaton.org

Extracted from `github.com/operaton/operaton.org` Jekyll source (`/assets/css/main.css`):

#### Colors

```js
// tailwind.config.js — colors
colors: {
  primary: {
    DEFAULT: '#184AEF',   // --color-primary (light mode)
    dark: '#0a2dbf',      // derived darker
    light: '#4a70f5',     // derived lighter
  },
  secondary: {
    DEFAULT: '#27F3E0',   // --color-secondary
  },
  neutral: {
    0:   '#ffffff',       // --color-bg
    50:  '#F5F0F3',       // --color-bg-2
    200: '#E3D4DD',       // --color-border
    500: '#666666',       // --color-font-secondary
    900: '#000000',       // --color-font
  },
}
```

#### Typography

```js
// tailwind.config.js — typography
fontFamily: {
  sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
  mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
},
fontSize: {
  'xs':   ['0.75rem',  { lineHeight: '1rem' }],
  'sm':   ['0.875rem', { lineHeight: '1.25rem' }],
  'base': ['1rem',     { lineHeight: '1.5rem' }],
  'lg':   ['1.125rem', { lineHeight: '1.75rem' }],
  'xl':   ['1.25rem',  { lineHeight: '1.75rem' }],
  '2xl':  ['1.5rem',   { lineHeight: '2rem' }],
  '3xl':  ['1.875rem', { lineHeight: '2.25rem' }],
},
```

#### Spacing

```js
// tailwind.config.js — spacing (extends defaults)
spacing: {
  'xs':  '0.25em',   // --space-xs
  's':   '0.5em',    // --space-s
  'm':   '1em',      // --space-m
  'l':   '2em',      // --space-l
  'xl':  '4em',      // --space-xl
},
borderRadius: {
  's':   '0.5em',    // --border-radius-s
  'DEFAULT': '0.5em',
},
maxWidth: {
  'content': '80rem',  // --max-content-width
},
```

#### Focus Ring (Accessibility)

Focus rings use `--color-primary` (#184AEF) as the outline color, meeting WCAG 2.1 AA contrast requirements against white backgrounds.

```css
/* Applied globally via Tailwind base layer */
*:focus-visible {
  outline: 2px solid #184AEF;
  outline-offset: 2px;
}
```

---

## 6. Visual Foundation

### Color Usage Rules

| Element | Color | Tailwind Class |
|---------|-------|----------------|
| Primary buttons | `#184AEF` | `bg-primary` |
| Button hover | `#0a2dbf` | `hover:bg-primary-dark` |
| Links | `#184AEF` | `text-primary` |
| Page background | `#ffffff` | `bg-neutral-0` |
| Section background | `#F5F0F3` | `bg-neutral-50` |
| Borders | `#E3D4DD` | `border-neutral-200` |
| Body text | `#000000` | `text-neutral-900` |
| Secondary text | `#666666` | `text-neutral-500` |
| Focus ring | `#184AEF` | `outline-primary` |
| Accent/badge | `#27F3E0` | `bg-secondary` |
| Success | `#16a34a` | `text-green-600` |
| Error | `#dc2626` | `text-red-600` |

### Typography Hierarchy

| Use | Font | Size | Weight |
|-----|------|------|--------|
| Page title / H1 | Inter | 3xl (1.875rem) | 700 |
| Section heading / H2 | Inter | 2xl (1.5rem) | 600 |
| Card title / H3 | Inter | xl (1.25rem) | 600 |
| Body | Inter | base (1rem) | 400 |
| Labels/captions | Inter | sm (0.875rem) | 500 |
| Code/file paths | JetBrains Mono | sm (0.875rem) | 400 |

### Spacing System

- Component internal padding: `p-4` (1rem) for cards, `p-3` (0.75rem) for form fields
- Section spacing: `py-12` (3rem) between major sections
- Grid gaps: `gap-6` (1.5rem) for gallery cards, `gap-4` (1rem) for form rows
- Content max-width: `max-w-content` (80rem), centered with `mx-auto`

---

## 7. Defining the Experience — Core Interaction Mechanics

### Route Architecture

```
/ (GalleryView)
  → user clicks a project type card
  → navigates to /configure?projectType=PROCESS_APPLICATION (or PROCESS_ARCHIVE)

/configure (ConfigureView)
  → form + live preview panel side-by-side
  → form state → query params (shareable URL)
  → Generate & Download / IDE deep-link buttons
```

### State Management (No Vuex/Pinia — composables only)

```
useMetadata()
  → MetadataResponse: { projectTypes[], buildSystems[], globalOptions }
  → isLoading: boolean
  → error: ProblemDetail | null

useProjectForm(metadata)
  → formState: reactive ProjectConfig
  → validation errors per field
  → computed fileTree (pure function of formState + metadata)
  → shareableUrl (computed from formState → URL query params)
  → initFromQuery(route.query) — restores state on load

useGenerate(formState)
  → isGenerating: boolean
  → error: ProblemDetail | null
  → generate() → triggers download
```

### File Tree Condition Evaluation

The file tree preview evaluates `templateManifest[].condition` strings as simple equality checks:

```ts
// Pure function — no server round-trip
function evaluateCondition(condition: string | null, state: ProjectConfig): boolean {
  if (!condition) return true;
  // condition examples: "buildSystem == 'MAVEN'", "dockerCompose == true"
  // Parsed as: field == value
  const [field, op, value] = condition.split(/\s+/);
  const rawValue = value.replace(/'/g, '');
  const stateVal = String(state[field as keyof ProjectConfig]);
  return op === '==' && stateVal === rawValue;
}
```

### Form Defaults

```ts
const defaults: ProjectConfig = {
  groupId: 'com.example',
  artifactId: 'my-process-app',
  projectName: 'My Process App',
  projectType: 'PROCESS_APPLICATION',
  buildSystem: 'MAVEN',
  javaVersion: 17,
  dependencyUpdater: 'RENOVATE',
  dockerCompose: false,
  githubActions: true,
}
```

### URL Sharing Scheme

Query parameters are human-readable and match field names from the OpenAPI spec:

```
/configure?projectType=PROCESS_APPLICATION&buildSystem=GRADLE_KOTLIN&groupId=org.myco&artifactId=my-app
```

On load, `useProjectForm.initFromQuery()` reads `route.query`, validates each param, applies valid ones over defaults, ignores unknown/invalid values silently.

---

## 8. Design Direction — Visual Layout

### Selected Direction: "Professional Developer Tool" (start.spring.io lineage)

**Rationale:** Operaton's target audience is developers. Developer tools that look like polished developer tools build trust. The spring.io aesthetic (clean, generous whitespace, single accent color, no decoration for decoration's sake) is the gold standard.

### App Shell Layout

```
┌─────────────────────────────────────────────────────┐
│ HEADER: Operaton logo + "Starter" wordmark + nav    │
├─────────────────────────────────────────────────────┤
│                                                     │
│              [ROUTE OUTLET]                         │
│                                                     │
├─────────────────────────────────────────────────────┤
│ FOOTER: operaton.org links, version, license        │
└─────────────────────────────────────────────────────┘
```

Header height: 64px fixed. Matches operaton.org header height.

### Gallery View Layout (`/`)

```
┌─────────────────────────────────────────────────────┐
│ HERO: "Start your Operaton project"                 │
│       Subtitle: one sentence explaining the tool    │
│       Two CTAs: [Browse Projects] [Configure Now →] │
├─────────────────────────────────────────────────────┤
│ PROJECT TYPES HEADING: "Choose a project type"      │
│                                                     │
│  ┌──────────────────┐  ┌──────────────────┐        │
│  │ PROCESS APP      │  │ PROCESS ARCHIVE  │        │
│  │ [icon]           │  │ [icon]           │        │
│  │ Display Name     │  │ Display Name     │        │
│  │ Description...   │  │ Description...   │        │
│  │ [tag][tag][tag]  │  │ [tag][tag][tag]  │        │
│  │ Persona hint     │  │ Persona hint     │        │
│  │ [?] More info    │  │ [?] More info    │        │
│  │                  │  │                  │        │
│  │ [Configure →]    │  │ [Configure →]    │        │
│  └──────────────────┘  └──────────────────┘        │
└─────────────────────────────────────────────────────┘
```

- Grid: `grid-cols-1 md:grid-cols-2 lg:grid-cols-3` (scales with more project types)
- Cards: `rounded-s border border-neutral-200 p-6 hover:shadow-md hover:border-primary transition-all`
- Tags: `inline-flex bg-secondary/20 text-primary text-xs font-medium px-2 py-1 rounded`
- "?" icon: `<button aria-label="Learn more about [ProjectType]">` expands inline accordion

### Configuration View Layout (`/configure`)

```
┌──────────────────────────────────────────────────────────────┐
│  ← Back to gallery                                           │
├─────────────────────────────┬────────────────────────────────┤
│  FORM PANEL (left)          │  PREVIEW PANEL (right)         │
│  min-w: 420px               │  flex: 1                       │
│  ─────────────────────────  │  ──────────────────────────    │
│  Project Identity           │  File Structure Preview        │
│  ┌─────────────────────┐    │  ┌──────────────────────────┐  │
│  │ Group ID       [?]  │    │  │ 📁 my-process-app/       │  │
│  │ com.example         │    │  │  ├─ pom.xml               │  │
│  ├─────────────────────┤    │  │  ├─ src/                  │  │
│  │ Artifact ID    [?]  │    │  │  │   └─ main/             │  │
│  │ my-process-app      │    │  │  │       └─ java/         │  │
│  ├─────────────────────┤    │  │  │  ├─ Application.java   │  │
│  │ Project Name   [?]  │    │  │  │  └─ ...                │  │
│  │ My Process App      │    │  └──────────────────────────┘  │
│  └─────────────────────┘    │                                │
│                             │  IDE Deep Links                │
│  Build Options              │  [Open in IntelliJ IDEA]       │
│  ┌─────────────────────┐    │  [Open in VS Code]             │
│  │ Project Type   [?]  │    │                                │
│  │ ○ Process App       │    │  Shareable URL                 │
│  │ ○ Process Archive   │    │  [Copy Link]                   │
│  ├─────────────────────┤    │                                │
│  │ Build System   [?]  │    │                                │
│  │ ○ Maven             │    │                                │
│  │ ○ Gradle (Groovy)   │    │                                │
│  │ ○ Gradle (Kotlin)   │    │                                │
│  ├─────────────────────┤    │                                │
│  │ Java Version   [?]  │    │                                │
│  │ ○ 17  ○ 21  ○ 25   │    │                                │
│  └─────────────────────┘    │                                │
│                             │                                │
│  Extras                     │                                │
│  ┌─────────────────────┐    │                                │
│  │ ☑ GitHub Actions[?] │    │                                │
│  │ ☐ Docker Compose[?] │    │                                │
│  │ Dep. Updates   [?]  │    │                                │
│  │ ○ Renovate ○ Dep.   │    │                                │
│  └─────────────────────┘    │                                │
│                             │                                │
│  [Generate & Download ↓]    │                                │
└─────────────────────────────┴────────────────────────────────┘
```

**Layout breakpoints:**
- `< 768px`: Single column — form above, preview below (collapsible)
- `≥ 768px`: Two-column layout as shown

**Form sections use `<fieldset>` + `<legend>` for semantic grouping**

---

## 9. User Journey Flows

### Journey 1: Practitioner (Returning Developer)

```
1. Arrives at /
2. Sees hero section with [Configure Now →] CTA
3. Clicks → navigated to /configure
4. Fills: groupId, artifactId, projectName
5. Keeps defaults (Maven, Java 17, RENOVATE, GitHub Actions on)
6. Watches file tree update as they type
7. Clicks [Generate & Download]
8. ZIP downloads as {artifactId}.zip
9. Total time: < 30 seconds ✓
```

**Key UX decisions for this journey:**
- "Configure Now" CTA in hero bypasses gallery entirely
- Defaults are correct for this persona — minimal interaction required
- Single-page form, no pagination or multi-step wizard

### Journey 2: Explorer (New to Operaton)

```
1. Arrives at /
2. Sees gallery cards
3. Hovers over "Process Application" card
4. Clicks [?] → inline accordion expands explaining the type
5. Reads persona hint: "Ideal for Camunda 7 migrators..."
6. Clicks [Configure →] on the card
7. Navigated to /configure?projectType=PROCESS_APPLICATION
8. Sees pre-selected project type
9. Reads help text for other fields via [?] icons
10. Configures and downloads
```

**Key UX decisions:**
- Gallery cards are generous in size — not a compact list
- Inline help (accordion) eliminates need to open docs
- Card click pre-selects → reduces form interaction needed

### Journey 3: Team Collaboration via Shareable URL

```
1. Developer A configures at /configure
2. Notices "Share Configuration" button in preview panel
3. Clicks → URL updates with query params
4. Copies URL, shares with teammate
5. Teammate opens URL
6. Form pre-fills from URL params
7. Teammate adjusts groupId, downloads
```

### Journey 4: IDE Integration

```
1. Developer configures form
2. Sees "Open in IntelliJ IDEA" button in preview panel
3. Clicks button
4. IDE opens (if installed), fetches ZIP, imports project
5. No download folder interaction needed
```

### Journey 5: Keyboard-Only User

```
1. Tabs to gallery → enters gallery cards
2. Arrow keys / Tab to navigate cards
3. Enter on card → navigates to /configure
4. Tab through form fields
5. Space to select radio options
6. Tab to [Generate & Download]
7. Enter to trigger download
8. Every step keyboard-accessible ✓
```

---

## 10. Component Strategy

### Component Hierarchy

```
App.vue
├── AppHeader.vue         — logo, nav, skip-link
├── AppFooter.vue         — links, version
└── [RouterView]
    ├── GalleryView.vue
    │   ├── HeroSection.vue      — headline, CTAs
    │   ├── ProjectTypeCard.vue  — card with expand
    │   └── ErrorBanner.vue      — shared error display
    └── ConfigureView.vue
        ├── ConfigForm.vue       — all form fields
        │   ├── IdentityFields.vue
        │   ├── BuildOptionsFields.vue
        │   └── ExtrasFields.vue
        ├── FileTreePreview.vue  — live preview panel
        ├── ActionPanel.vue      — download + IDE + share
        └── ErrorBanner.vue      — shared error display
```

### Custom Component Specifications

#### `<ProjectTypeCard>`

```html
<article
  class="rounded-s border border-neutral-200 p-6 hover:shadow-md hover:border-primary transition-all cursor-pointer"
  :aria-label="projectType.displayName"
  tabindex="0"
  @click="handleSelect"
  @keydown.enter="handleSelect"
>
  <h3 class="text-xl font-semibold text-neutral-900">{{ projectType.displayName }}</h3>
  <p class="mt-2 text-neutral-500 text-sm">{{ projectType.description }}</p>

  <div class="mt-3 flex flex-wrap gap-2">
    <span v-for="tag in projectType.tags"
      class="inline-flex bg-secondary/20 text-primary text-xs font-medium px-2 py-1 rounded">
      {{ tag }}
    </span>
  </div>

  <p class="mt-3 text-sm text-primary font-medium">{{ projectType.personaHint }}</p>

  <!-- Expandable help -->
  <button @click.stop="toggleHelp" :aria-expanded="helpOpen"
    class="mt-3 text-sm text-neutral-500 hover:text-primary flex items-center gap-1">
    <span class="text-xs">?</span> More about this project type
  </button>
  <div v-show="helpOpen" class="mt-2 text-sm text-neutral-500 border-t border-neutral-200 pt-2">
    {{ projectType.description }}
  </div>

  <button @click.stop="handleSelect"
    class="mt-4 w-full bg-primary text-white py-2 rounded-s font-medium hover:bg-primary-dark">
    Configure →
  </button>
</article>
```

#### `<FileTreePreview>`

```html
<section aria-label="File structure preview" aria-live="polite">
  <h2 class="text-sm font-semibold text-neutral-500 uppercase tracking-wide mb-3">
    File Structure
  </h2>
  <div class="font-mono text-sm bg-neutral-50 rounded-s p-4 border border-neutral-200">
    <ul role="tree">
      <FileTreeNode v-for="node in treeNodes" :key="node.path" :node="node" />
    </ul>
  </div>
</section>
```

The tree is computed client-side from `metadata.projectTypes[selectedType].templateManifest` filtered by condition evaluation and identity interpolation.

#### `<HelpIcon>` (Inline Help Pattern)

Every configuration field uses a consistent help icon pattern:

```html
<div class="flex items-center gap-1">
  <label class="text-sm font-medium text-neutral-900">{{ label }}</label>
  <button
    type="button"
    :aria-label="`Help: ${label}`"
    :aria-expanded="expanded"
    @click="expanded = !expanded"
    class="w-4 h-4 rounded-full bg-neutral-200 text-neutral-500 text-xs
           flex items-center justify-center hover:bg-primary hover:text-white
           focus-visible:outline-2 focus-visible:outline-primary"
  >?</button>
</div>
<div v-show="expanded" role="note" class="mt-1 text-sm text-neutral-500 bg-neutral-50 p-2 rounded-s">
  <slot name="help" />
</div>
```

#### `<ErrorBanner>`

```html
<div v-if="error" role="alert" aria-live="assertive"
  class="rounded-s border border-red-200 bg-red-50 p-4 flex gap-3">
  <span class="text-red-600">⚠</span>
  <div>
    <p class="font-medium text-red-700">{{ error.title }}</p>
    <p class="text-sm text-red-600 mt-1">{{ error.detail }}</p>
  </div>
</div>
```

#### `<SkeletonCard>` (Loading State)

```html
<div class="rounded-s border border-neutral-200 p-6 animate-pulse">
  <div class="h-6 bg-neutral-200 rounded w-3/4 mb-3"></div>
  <div class="h-4 bg-neutral-200 rounded w-full mb-2"></div>
  <div class="h-4 bg-neutral-200 rounded w-5/6 mb-4"></div>
  <div class="flex gap-2 mb-4">
    <div class="h-6 bg-neutral-200 rounded w-16"></div>
    <div class="h-6 bg-neutral-200 rounded w-20"></div>
  </div>
  <div class="h-10 bg-neutral-200 rounded w-full"></div>
</div>
```

### IDE Deep-Link Specification

#### IntelliJ IDEA Deep-Link

```
idea://com.intellij.ide.starter?url={encoded_generate_url}
```

Where `{encoded_generate_url}` is the full `POST /api/v1/generate` URL with body encoded as query params. IDEA fetches the ZIP and imports via Spring Initializr protocol.

**Implementation:**
```ts
function buildIntelliJUrl(baseUrl: string, config: ProjectConfig): string {
  const generateUrl = `${baseUrl}/api/v1/generate`;
  const params = new URLSearchParams(Object.entries(config).map(([k, v]) => [k, String(v)]));
  return `idea://com.intellij.ide.starter?url=${encodeURIComponent(`${generateUrl}?${params}`)}`;
}
```

#### VS Code Deep-Link

```
vscode://vscjava.vscode-spring-initializr/open?url={encoded_generate_url}
```

Or alternatively, trigger download and open with a documented workflow instruction (VS Code doesn't have native project-import deep-link as of 2026).

---

## 11. UX Consistency Patterns

### Form Field Pattern

All form fields follow the same structure:

```
┌─ Label + [?] Help Icon ─────────────────┐
│  <input> or <select> or <radio group>   │
│  [Error message if invalid]             │
└─────────────────────────────────────────┘
```

- Labels always above inputs (never floating/placeholder-as-label)
- Error messages use `role="alert"` or tied via `aria-describedby`
- All inputs have `autocomplete` attributes where applicable

### Validation Rules (mirror OpenAPI spec)

| Field | Rule | Error Message |
|-------|------|---------------|
| groupId | `[a-z][a-z0-9.]*` | "Use lowercase letters, numbers, and dots only" |
| artifactId | `[a-z][a-z0-9-]*` | "Use lowercase letters, numbers, and hyphens only" |
| projectName | Non-empty, max 100 chars | "Project name is required" |

### Loading States

| State | UI Treatment |
|-------|-------------|
| Metadata loading | Skeleton cards in gallery, form shimmer |
| Generation in progress | Button shows spinner + "Generating..." text; button disabled |
| Download complete | Transient success message ("Downloaded {artifactId}.zip") |

### Transition Patterns

- Route transitions: `fade` (150ms) — fast, non-distracting
- Accordion expand: `max-height` transition (200ms ease-out)
- Preview update: instant (pure computed property, no animation needed)
- Card hover: `shadow` + `border-primary` transition (150ms)

### Button Hierarchy

| Type | Style | Use |
|------|-------|-----|
| Primary | `bg-primary text-white` | Generate & Download, Configure → |
| Secondary | `border border-primary text-primary` | Copy Link, Open in IDE |
| Ghost | `text-neutral-500 hover:text-primary` | Back, Cancel |

---

## 12. Responsive Design & Accessibility

### Breakpoints (Tailwind defaults)

| Breakpoint | Width | Layout change |
|------------|-------|---------------|
| Mobile | < 768px | Single column; form then preview below |
| Tablet | ≥ 768px | Two-column layout in configure view |
| Desktop | ≥ 1024px | Two-column with wider preview panel |
| Wide | ≥ 1280px | Columns capped at `max-w-content` (80rem) |

### Mobile Adaptations

- Gallery: `grid-cols-1` → full-width cards
- Configure: Preview panel collapses to disclosure (`<details>`) below form
- Header: hamburger menu for navigation (if nav items added)
- Buttons: min-height 44px (WCAG 2.5.5 target size)

### Accessibility Requirements (WCAG 2.1 AA)

#### Focus Management
- Global focus ring: `outline: 2px solid #184AEF; outline-offset: 2px`
- All focusable elements: visible focus ring at all times when keyboard-focused
- Tab order: left-to-right, top-to-bottom; matches visual layout

#### Skip Navigation
```html
<a href="#main-content" class="sr-only focus:not-sr-only focus:absolute focus:top-4 focus:left-4
   bg-primary text-white px-4 py-2 rounded-s z-50">
  Skip to main content
</a>
```

#### ARIA Patterns

| Component | ARIA Pattern |
|-----------|-------------|
| Gallery card (clickable) | `role="article"`, `tabindex="0"`, `aria-label` |
| File tree | `role="tree"` > `role="treeitem"` |
| Live preview | `aria-live="polite"` on wrapper |
| Error messages | `role="alert"` or `aria-describedby` |
| Loading state | `aria-busy="true"` on container |
| Help accordion | `aria-expanded` on button, `aria-controls` on content |
| Radio groups | `<fieldset>` + `<legend>` |

#### Color Contrast Ratios

| Combination | Ratio | WCAG Level |
|-------------|-------|------------|
| `#184AEF` on `#ffffff` | 5.9:1 | AA ✓ |
| `#000000` on `#ffffff` | 21:1 | AAA ✓ |
| `#666666` on `#ffffff` | 5.7:1 | AA ✓ |
| White on `#184AEF` | 5.9:1 | AA ✓ |

#### Keyboard Navigation Map

| Key | Action |
|-----|--------|
| `Tab` | Move to next focusable element |
| `Shift+Tab` | Move to previous focusable element |
| `Enter` | Activate focused button/card |
| `Space` | Toggle checkbox / activate button |
| `Arrow keys` | Navigate radio button groups |
| `Escape` | Close open accordion/help panels |

### axe-core CI Integration

The `lint-web` CI job (GitHub Actions) includes:

```yaml
- name: Accessibility audit
  run: npm run test:a11y
```

```ts
// vitest accessibility test
import { axe } from 'vitest-axe';
// Runs axe against rendered GalleryView and ConfigureView
// Zero violations = pass; any violation = build fails
```

---

## 13. Implementation Notes for Story 4.1

### Tailwind Configuration

```js
// starter-web/tailwind.config.js
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts}'],
  theme: {
    extend: {
      colors: {
        primary: { DEFAULT: '#184AEF', dark: '#0a2dbf', light: '#4a70f5' },
        secondary: { DEFAULT: '#27F3E0' },
        neutral: { 0: '#ffffff', 50: '#F5F0F3', 200: '#E3D4DD', 500: '#666666', 900: '#000000' },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
      borderRadius: { s: '0.5em' },
      maxWidth: { content: '80rem' },
    },
  },
}
```

**CRITICAL**: All Tailwind classes must be static strings. No dynamic class construction.

### Vite Build Output Configuration

```ts
// starter-web/vite.config.ts
export default defineConfig({
  build: {
    outDir: '../starter-server/src/main/resources/static',
    emptyOutDir: true,
  },
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
```

### OpenAPI Client Generation

The web UI uses the OpenAPI-generated TypeScript client from `src/generated/`:

```json
// starter-web/package.json (excerpt)
"scripts": {
  "generate:api": "openapi-generator-cli generate -i ../openapi.yaml -g typescript-fetch -o src/generated"
}
```

---

## 14. Design Decisions Log

| Decision | Chosen | Rationale |
|----------|--------|-----------|
| CSS framework | Tailwind CSS | Stack already set; operaton.org token compatibility |
| State management | Composables only | No Pinia (per Story 1.1 AC); simpler for this scope |
| Form layout | Single-page | No wizard; Practitioner needs speed |
| Gallery layout | Grid cards | code.quarkus.io pattern; scales to more project types |
| Preview update | Client-side computed | NFR2: ≤200ms; no server round-trip |
| Condition evaluation | Simple string parser | Conditions are simple equality; no eval() |
| Error display | Shared `<ErrorBanner>` | Consistent; no per-component error handling |
| Loading state | Skeleton cards | No layout shift on load |
| Dark mode | Not in MVP | Scope control; light mode only for V1 |
| Route transitions | Fade 150ms | Non-distracting; fast |
| Shareable URL | Query params | Human-readable; no encoding overhead |

---

## UX Requirements Coverage

| Requirement | Covered By |
|-------------|------------|
| UX-DR1: Two entry points (form + gallery) | GalleryView + ConfigureView routes |
| UX-DR2: Gallery cards with metadata | `<ProjectTypeCard>` driven by `useMetadata` |
| UX-DR3: Inline contextual help | `<HelpIcon>` on every field; card [?] expand |
| UX-DR4: Live preview ≤200ms, client-side | `FileTreePreview` — computed from form state |
| UX-DR5: Keyboard-complete flow | ARIA map + focus management section |
| UX-DR6: IDE deep-links | `<ActionPanel>` with IntelliJ + VS Code buttons |
| UX-DR7: Shareable config URL | `useProjectForm.shareableUrl` + "Copy Link" |
| UX-DR8: operaton.org design tokens | Section 5 & 6 — Tailwind config with extracted tokens |
| UX-DR9: Browser support (2 major versions) | Tailwind + Vue 3 baseline |
| UX-DR10: WCAG 2.1 AA | Section 12 — axe-core CI validation |
| UX-DR11: Professional, polished, delightful | "Professional Developer Tool" direction; spring.io lineage |
