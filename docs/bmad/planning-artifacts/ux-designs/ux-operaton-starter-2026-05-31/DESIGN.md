---
name: operaton-starter
description: Open-source Operaton project generator at start.operaton.org
status: final
updated: 2026-06-01
sources:
  - imports/ux-design-specification.md
  - imports/ux-color-themes.html
  - imports/ux-design-directions.html
colors:
  primary: '#184AEF'
  primary-dark: '#0a2dbf'
  primary-light: '#4a70f5'
  secondary: '#27F3E0'
  neutral-0: '#ffffff'
  neutral-50: '#F5F0F3'
  neutral-200: '#E3D4DD'
  neutral-500: '#666666'
  neutral-900: '#000000'
  success: '#16a34a'
  error: '#b91c1c'
  error-bg: '#fef2f2'
  error-border: '#fecaca'
typography:
  sans:
    fontFamily: 'Inter, system-ui, -apple-system, sans-serif'
  mono:
    fontFamily: 'JetBrains Mono, Fira Code, monospace'
  heading-1:
    fontFamily: 'Inter'
    fontSize: 1.875rem
    fontWeight: '700'
    lineHeight: '2.25rem'
  heading-2:
    fontFamily: 'Inter'
    fontSize: 1.5rem
    fontWeight: '600'
    lineHeight: '2rem'
  heading-3:
    fontFamily: 'Inter'
    fontSize: 1.25rem
    fontWeight: '600'
    lineHeight: '1.75rem'
  body:
    fontFamily: 'Inter'
    fontSize: 1rem
    fontWeight: '400'
    lineHeight: '1.5rem'
  label:
    fontFamily: 'Inter'
    fontSize: 0.875rem
    fontWeight: '500'
    lineHeight: '1.25rem'
  code:
    fontFamily: 'JetBrains Mono, Fira Code, monospace'
    fontSize: 0.875rem
    fontWeight: '400'
    lineHeight: '1.25rem'
rounded:
  s: 0.5em
  DEFAULT: 0.5em
spacing:
  xs: 0.25em
  s: 0.5em
  m: 1em
  l: 2em
  xl: 4em
  content-max-width: 80rem
  header-height: 4rem
components:
  button-primary:
    background: '{colors.primary}'
    foreground: '{colors.neutral-0}'
    radius: '{rounded.s}'
    hover-background: '{colors.primary-dark}'
  button-secondary:
    background: 'transparent'
    foreground: '{colors.primary}'
    border: '1px solid {colors.primary}'
    radius: '{rounded.s}'
  button-ghost:
    background: 'transparent'
    foreground: '{colors.neutral-500}'
    hover-foreground: '{colors.primary}'
  tag:
    background: '{colors.secondary}/20'
    foreground: '{colors.primary}'
    fontSize: 0.75rem
    fontWeight: '500'
    radius: 0.25em
    padding: '0.25rem 0.5rem'
  card:
    background: '{colors.neutral-0}'
    border: '1px solid {colors.neutral-200}'
    radius: '{rounded.s}'
    padding: 1.5rem
    hover-shadow: '0 4px 12px rgba(0,0,0,0.08)'
    hover-border: '{colors.primary}'
  input:
    background: '{colors.neutral-0}'
    border: '1px solid {colors.neutral-200}'
    radius: '{rounded.s}'
    padding: '0.75rem'
    focus-border: '{colors.primary}'
  skeleton:
    background: '{colors.neutral-200}'
    animation: pulse
  error-banner:
    background: '{colors.error-bg}'
    border: '1px solid {colors.error-border}'
    foreground: '{colors.error}'
    radius: '{rounded.s}'
  focus-ring:
    outline: '2px solid {colors.primary}'
    outline-offset: 2px
  help-icon:
    background: '{colors.neutral-200}'
    foreground: '{colors.neutral-500}'
    hover-background: '{colors.primary}'
    hover-foreground: '{colors.neutral-0}'
    size: 16px
    touch-target: 44px
  action-panel:
    background: '{colors.neutral-50}'
    border: '1px solid {colors.neutral-200}'
    radius: '{rounded.s}'
    padding: '1rem'
  file-tree-preview:
    background: '{colors.neutral-50}'
    border: '1px solid {colors.neutral-200}'
    radius: '{rounded.s}'
    padding: '1rem'
    font: '{typography.code}'
    item-hover-background: '{colors.neutral-200}'
    item-selected-background: '{colors.primary}/10'
    item-selected-foreground: '{colors.primary}'
    item-selected-border-left: '2px solid {colors.primary}'
  file-content-pane:
    background: '{colors.neutral-50}'
    border: '1px solid {colors.neutral-200}'
    radius: '{rounded.s}'
    padding: '1rem'
    font: '{typography.code}'
    filename-foreground: '{colors.neutral-500}'
    filename-fontSize: 0.75rem
  conditional-sub-option:
    background: '{colors.neutral-50}'
    border-left: '2px solid {colors.neutral-200}'
    padding-left: '1rem'
    margin-top: '0.5rem'
---

## Brand & Style

Operaton Starter belongs visually to the Operaton ecosystem. The design is derived directly from the `operaton.org` design tokens (extracted from the Jekyll source at `github.com/operaton/operaton.org`). The visual direction is **"Professional Developer Tool"** — the same lineage as `start.spring.io`.

Primary inspirations:

| Source | What is adopted |
|--------|----------------|
| `start.spring.io` | Single-page layout, left-form / right-preview split, generous whitespace, minimal color, immediate feedback |
| `code.quarkus.io` | Gallery card grid, card anatomy (title + description + tags + persona hint), search/filter patterns |
| `operaton.org` | Exact color palette, typography, header/footer visual language, border radius and spacing tokens |

The result is a tool that feels like it was built by the same team that built `operaton.org` — familiar to ecosystem users, polished enough to build trust with newcomers.

Design direction explorations are in `imports/ux-design-directions.html`.

## Colors

All colors are extracted from `operaton.org` CSS custom properties.

| Token | Value | Purpose |
|-------|-------|---------|
| `primary` | `#184AEF` | Buttons, links, focus rings, selected-state borders — the single active-interaction color |
| `primary-dark` | `#0a2dbf` | Button hover state only |
| `primary-light` | `#4a70f5` | Focus ring on dark or colored surfaces (reserved; not used in MVP light-mode-only scope) |
| `secondary` | `#27F3E0` | Accent only — used at 20% opacity for tag backgrounds. Never used for text or interactive elements |
| `neutral-0` | `#ffffff` | Page background, card background, input background |
| `neutral-50` | `#F5F0F3` | Alternating section backgrounds, code preview background, help panel background |
| `neutral-200` | `#E3D4DD` | All borders (cards, inputs, dividers), skeleton placeholder fill |
| `neutral-500` | `#666666` | Secondary text (descriptions, captions, placeholder labels) |
| `neutral-900` | `#000000` | Primary body text |
| `success` | `#16a34a` | Transient success messages only (e.g., "Downloaded {artifactId}.zip") |
| `error` | `#b91c1c` | Error banner text; validation error messages (darkened from #dc2626 for WCAG AA compliance on light error background) |
| `error-bg` | `#fef2f2` | Error banner background |
| `error-border` | `#fecaca` | Error banner border |

**Usage rules:**
- Use `primary` for one thing at a time per visual region — avoid two adjacent primary-colored elements.
- `secondary` (#27F3E0) is never used at full opacity in UI; always apply at `/20` (20% opacity) for tag badges.
- Never use color as the only signal for error or success — always pair with text.

**Contrast ratios (WCAG 2.1 AA verified):**

| Pair | Ratio | Level |
|------|-------|-------|
| `primary` (#184AEF) on `neutral-0` (#ffffff) | 5.9:1 | AA ✓ |
| `neutral-900` (#000000) on `neutral-0` (#ffffff) | 21:1 | AAA ✓ |
| `neutral-500` (#666666) on `neutral-0` (#ffffff) | 5.7:1 | AA ✓ |
| White (#ffffff) on `primary` (#184AEF) | 5.9:1 | AA ✓ |
| `neutral-500` (#666666) on `neutral-50` (#F5F0F3) | 4.6:1 | AA ✓ (large text; use at ≥14px bold or ≥18px regular only) |
| `primary` (#184AEF) on tag bg (secondary at 20% over white ≈ #d4faf7) | 4.7:1 | AA ✓ (requires tag text ≥14px bold or ≥18px regular; current 12px/500 is marginal — prefer `primary-dark` for tag text at 12px) |
| `error` (#b91c1c) on `error-bg` (#fef2f2) | 5.1:1 | AA ✓ |

Color palette explorations are in `imports/ux-color-themes.html`.

## Typography

Inter is used throughout. JetBrains Mono is reserved exclusively for code and file paths.

| Role | Token | Size | Weight | Usage |
|------|-------|------|--------|-------|
| Page title | `heading-1` | 1.875rem | 700 | Hero headline only |
| Section heading | `heading-2` | 1.5rem | 600 | "Choose a project type", panel headings |
| Card title | `heading-3` | 1.25rem | 600 | ProjectTypeCard display name |
| Body | `body` | 1rem | 400 | Descriptions, prose |
| Label / caption | `label` | 0.875rem | 500 | Form field labels, small section labels |
| Code / file path | `code` | 0.875rem | 400 | File tree preview, inline code |

Labels are always rendered above inputs — never as floating labels or placeholder-as-label.

## Layout & Spacing

- **Content max-width**: `80rem` — centered with `mx-auto`, applied to the inner content container on all views.
- **Header height**: `min-height: 4rem` — matches operaton.org; uses `rem` to survive 200% text zoom.
- **Section vertical rhythm**: `py-12` (3rem) between major page sections.
- **Grid gaps**: `gap-6` (1.5rem) for gallery card grid; `gap-4` (1rem) for form rows.
- **Card internal padding**: `p-6` (1.5rem).
- **Input internal padding**: `p-3` (0.75rem).
- **Gallery grid**: `grid-cols-1 md:grid-cols-2 lg:grid-cols-3` — scales as project types are added.
- **Configure view**: two-column split at `≥768px` — form panel (`min-w: 420px`) left, preview panel (`flex: 1`) right; single column below 768px.

## Elevation & Depth

Shadows are used sparingly — only for hover elevation on interactive cards.

- **Default card**: no shadow; `border: 1px solid {colors.neutral-200}`.
- **Card hover**: `box-shadow: 0 4px 12px rgba(0,0,0,0.08)` + border transitions to `{colors.primary}`.
- **Transition**: `transition-all 150ms` on hover state changes.
- No floating panels, modals, or overlapping layers in the MVP design.

## Shapes

All interactive elements use `border-radius: 0.5em` (`rounded-s`). This single radius value is applied consistently to: cards, buttons, inputs, tags, help panels, error banners, skeleton placeholders, and the skip-nav link. Tag chips use a slightly tighter `0.25em` radius to distinguish them as inline badges.

## Components

### Button hierarchy

Three levels, used in strict order of visual weight:

| Token | Visual | Used for |
|-------|--------|----------|
| `button-primary` | Filled blue, white text | "Generate & Download", "Configure →" — one per primary action zone |
| `button-secondary` | Transparent, blue border and text | "Copy Link", "Open in IntelliJ IDEA", "Open in VS Code" |
| `button-ghost` | No border, gray text → blue on hover | "← Back to gallery", cancel/dismiss actions |

Minimum button height: 44px (WCAG 2.5.5 target size).

### Card (`{components.card}`)

Used for ProjectTypeCard (gallery) and SkeletonCard (loading). Default state: white background, `neutral-200` border, `rounded-s`, `p-6`. Hover: border transitions to `primary`, shadow lifts to `hover-shadow`. The border color change (not just shadow) signals interactivity without relying on shadow alone.

### Tag (`{components.tag}`)

Inline badge rendered inside cards. `secondary` at 20% opacity as background, `primary` as text. Used for project type metadata keywords (e.g., "BPMN", "Spring Boot"). Never used for interactive filtering in MVP.

### Input (`{components.input}`)

Text inputs and select fields: white background, `neutral-200` border, `rounded-s`, `p-3` internal padding. On focus: border color transitions to `primary`, focus ring applied globally via `*:focus-visible` rule. Labels always above.

### Error Banner (`{components.error-banner}`)

Full-width banner inside the view (not a toast). `{colors.error-bg}` background, `{colors.error-border}` border, `{colors.error}` text. Includes a warning icon (⚠) and two text lines: `error.title` (medium weight) + `error.detail` (small, lighter). `role="alert"` + `aria-live="assertive"` ensures screen readers announce immediately.

### Skeleton Card (`{components.skeleton}`)

Matches the card layout exactly — same padding and border — with `neutral-200` fill blocks replacing text and a CSS `animate-pulse` animation. Prevents layout shift when metadata loads.

### Focus Ring (`{components.focus-ring}`)

Applied globally via `*:focus-visible`: `outline: 2px solid {colors.primary}; outline-offset: 2px`. Never suppressed with `outline: none` unless a custom visible alternative is provided.

### HelpIcon (`{components.help-icon}`)

Small circular `?` button adjacent to form field labels. Visible size 16×16px; touch target padded to 44×44px (WCAG 2.5.5). Default: `{colors.neutral-200}` background, `{colors.neutral-500}` text. Hover/focus: `{colors.primary}` background, white text. Focus ring via `{components.focus-ring}`.

### ActionPanel (`{components.action-panel}`)

Container in the right panel of ConfigureView, below the file tree preview. `{colors.neutral-50}` background, `{colors.neutral-200}` border, `{rounded.s}` corners, `1rem` internal padding. Groups IDE deep-link buttons, shareable URL copy, and the primary Generate & Download button.

### FileTreePreview (`{components.file-tree-preview}`)

Interactive file structure display. `{colors.neutral-50}` background, `{colors.neutral-200}` border, `{rounded.s}` corners, `1rem` padding. Font: `{typography.code}`. Items are clickable — hover state uses `{components.file-tree-preview.item-hover-background}`; the selected file item gains a left-border accent (`{components.file-tree-preview.item-selected-border-left}`) and background tint (`{components.file-tree-preview.item-selected-background}`).

### FileContentPane (`{components.file-content-pane}`)

Adjacent content pane that shows the representative source content of the selected file from the file tree. `{colors.neutral-50}` background, `{colors.neutral-200}` border, `{rounded.s}` corners, `1rem` padding. Font: `{typography.code}`. A filename label above the content uses `{components.file-content-pane.filename-foreground}` and `{components.file-content-pane.filename-fontSize}`. Content is static template source (`TemplateManifestEntry.previewContent`) — it does not dynamically reflect the current form state.

### ConditionalSubOption (`{components.conditional-sub-option}`)

Revealed below a parent option when the parent is enabled (e.g., Gradle DSL after Gradle is selected; Dependency Updates flavour after the checkbox is checked). Uses a left-border indent visual (`{components.conditional-sub-option.border-left}`, `{components.conditional-sub-option.padding-left}`) and neutral-50 background to convey hierarchical relationship. Animated reveal: `max-height` transition 200ms ease-out (same as help accordion). Hidden entirely when the parent is unselected.

## Do's and Don'ts

| Do | Don't |
|----|-------|
| Use `primary` for buttons, links, and focus rings | Use `secondary` (#27F3E0) at full opacity in UI |
| Use skeleton cards during metadata load | Show a spinner in place of the card grid (causes layout shift) |
| Apply `transition-all 150ms` on card hover | Use slow transitions (>200ms) on interactive elements |
| Use static Tailwind class strings | Construct Tailwind class names dynamically at runtime (breaks JIT purge) |
| Render labels above inputs | Use placeholder text as the only label |
| Use the shared `<ErrorBanner>` component | Display per-field inline errors for API/network failures |
| Apply focus ring via global `*:focus-visible` | Remove `outline` without a custom visible focus alternative |
| Use `neutral-50` for code preview backgrounds | Use white-on-white for code areas (no contrast) |
| Keep route transitions at `fade 150ms` | Use sliding/bouncing transitions (distracting for a tool) |
| Use `<fieldset>` + `<legend>` for radio groups | Group radio buttons with only visual styling |
| Apply `@media (prefers-reduced-motion: reduce)` to suppress all transitions and `animate-pulse` | Animate skeleton cards and route transitions for users with `prefers-reduced-motion` active |
| Test tag and error-banner colors in Windows High Contrast Mode (forced-colors) | Rely solely on background-color opacity for visual differentiation in forced-color contexts |
