---
title: "Examples Gallery v1 — UI Interaction Specification"
status: draft
created: 2026-06-15
purpose: "Design clarity for frontend dev before Epic 2 & 3 coding begins"
---

# Examples Gallery v1 — UI Interaction Specification

**Purpose:** Formalize interaction affordances, error states, and mobile constraints so frontend devs can code without ambiguity on button behavior, icon placement, and loading states.

---

## 1. Details Toggle ("?" Button)

### Requirement
Card displays compact view by default. Expanded detail view reveals long description, integrations, concepts, authors, license, and metadata — only on explicit user interaction.

### Design Spec

**Button Element:**
- **Type:** Semantic `<button>` with `type="button"`
- **Label:** Question mark icon (?) — NO text label
- **Icon:** Unicode `?` character, style as 16px, font-weight 600, or use SVG circle-with-question-mark glyph
- **Position:** Top-right corner of card header, inline with title row (right-align)
- **Size:** 24×24px minimum touch target (WCAG AA)

**Styling — Collapsed State:**
```css
background: transparent;
border: 1px solid transparent;
color: rgb(102, 102, 102);    /* neutral-500 */
cursor: pointer;
transition: all 150ms ease;
```

**Styling — Hover State:**
```css
color: rgb(24, 74, 239);      /* primary-blue */
border: 1px solid rgb(24, 74, 239);
border-radius: 4px;
background: rgba(24, 74, 239, 0.05);
```

**Styling — Focus State (Keyboard):**
```css
outline: 2px solid rgb(24, 74, 239);
outline-offset: 2px;
```

**Styling — Expanded State:**
```css
color: rgb(24, 74, 239);
border: 1px solid rgb(24, 74, 239);
background: rgba(24, 74, 239, 0.05);
transform: rotate(180deg);          /* or use ▼ icon instead of ? */
transition: transform 150ms ease;
```

**Attributes:**
- `aria-expanded="true|false"` — Update based on `detailsOpen` state
- `aria-controls="details-{example-id}"` — Match the detail panel ID
- `title="Show details"` — Hover tooltip

**Keyboard Interaction:**
- **Enter** or **Space** toggles expanded state
- **Escape** (when focused in expanded detail view) collapses details

**Implementation Note:**
Current `ExampleGalleryCard.vue` has toggle logic. Update template:
```vue
<button
  v-if="hasDetails"
  type="button"
  class="details-toggle-button"
  :aria-expanded="detailsOpen"
  :aria-controls="`details-${example.id}`"
  title="Show details"
  @click="toggleDetails"
  @keydown.enter="toggleDetails"
  @keydown.space.prevent="toggleDetails"
  @keydown.escape="detailsOpen = false"
>
  ?
</button>
```

---

## 2. Download Error Handling (⚠️ Warning Icon)

### Requirement
When example is missing/broken at pinned SHA (`isDownloadable: false`), the Download button is disabled and a warning icon provides an escape hatch: clicking it opens the source repository so users can report the issue.

### Design Spec

**Button State — Enabled (Normal):**
```
[📥 Download]  [View on GitHub →]
```

**Button State — Disabled (Example Missing):**
```
[📥 Download]  ⚠️  [View on GitHub →]
```

**Button Element (When Disabled):**
- **Appearance:** Same size as enabled, but:
  - Background: `rgb(229, 229, 229)` (neutral-300)
  - Text color: `rgb(153, 153, 153)` (neutral-400)
  - Cursor: `not-allowed`
- **Interaction:** Disabled; click does nothing
- **Tooltip:** None (icon provides feedback)

**Warning Icon:**
- **Type:** Unicode ⚠️ emoji OR SVG triangle-with-exclamation (16×16px)
- **Position:** To the RIGHT of Download button, inline in the action row
- **Size:** 20×20px
- **Color:** `rgb(217, 119, 6)` (amber-600)
- **Interactive:** `<button>` or `<a>` element (clickable)
- **Click behavior:** Opens `example.sourceRepoUrl` in new tab
- **Cursor:** `pointer`

**Warning Icon Styling:**
```css
background: transparent;
border: 1px solid transparent;
color: rgb(217, 119, 6);      /* amber-600 */
transition: all 150ms ease;

&:hover {
  background: rgba(217, 119, 6, 0.1);
  border-color: rgb(217, 119, 6);
}

&:focus {
  outline: 2px solid rgb(217, 119, 6);
  outline-offset: 2px;
}
```

**Tooltip (Hover):**
```
"Example not found in source repo. Report issue →"
```

**Implementation Note:**
The DownloadAction component needs:
1. Conditional rendering of warning icon based on `downloadStatus.isAvailable`
2. Link wrapping: `<a :href="example.sourceRepoUrl" target="_blank" rel="noopener noreferrer">`
3. Tooltip via `title` attribute or separate `<span>` with aria-describedby

---

## 3. Download Progress UI

### Requirement
While ZIP download is in progress, user receives visual feedback. On failure, error message is displayed with retry option.

### Design Spec

**Download Button — In Progress State:**

Text transforms to "Downloading..." + inline spinner icon:
```
[⟳ Downloading...]  [View on GitHub →]
```

**Spinner Styling:**
- **Animation:** Rotate 360° continuously, 800ms per rotation
- **Icon:** SVG circle with partial stroke (like a loading indicator)
  - OR use Unicode ↻ character with rotation
- **Color:** `rgb(24, 74, 239)` (primary-blue)
- **Size:** 16×16px, inline with text

**Implementation:**
```vue
<button 
  type="button"
  :disabled="downloadStatus.isLoading"
  class="download-button"
>
  <span v-if="downloadStatus.isLoading" class="spinner">⟳</span>
  <span>{{ downloadStatus.isLoading ? 'Downloading...' : 'Download' }}</span>
</button>

<style>
.spinner {
  display: inline-block;
  animation: spin 0.8s linear infinite;
  margin-right: 0.5rem;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
```

**Error State — Download Failed:**

After failure (502, 504, timeout), button returns to normal state and error toast appears:

```
✗ Download failed: Network error. Try again?
[Dismiss] [Retry]
```

**Toast Styling:**
- **Background:** `rgb(239, 68, 68)` (red-600)
- **Text:** White
- **Position:** Bottom-left corner (mobile-friendly; not bottom-right which hides on small screens)
- **Duration:** 6 seconds auto-dismiss, OR manual dismiss button
- **Actions:** 
  - Dismiss: Closes toast
  - Retry: Calls the same download handler again

**Toast Message Copy:**
- Network failure: "Download failed: Network error. Try again?"
- 502 Bad Gateway: "GitHub unreachable. Try again?"
- 413 Payload Too Large: "Example is too large to download (exceeds 50 MB)."
- Timeout: "Download took too long. Try again?"

**Implementation Note:**
`useExampleDownload.ts` needs to emit error states. Toast component wraps entire GalleryView.

---

## 4. Mobile Layout — 320px Constraint

### Requirement
Card layout adapts to 320px viewport width (iPhone SE, small Android phones). No overflow, no text truncation surprises.

### Design Spec

**Card Layout at 320px:**
```
┌─────────────────────────┐
│ [🎨]  Title             │ ← Icon (48×48px), title truncates
│       Short description │ ← Full width, may wrap
├─────────────────────────┤
│ [tag] [badge] [badge]   │ ← Tags wrap, single row preferred
├─────────────────────────┤
│ [⚠️ Download] [View...] │ ← Button text stacks or shrinks
└─────────────────────────┘
```

**Breakpoint Details:**

| Viewport | Icon | Title | Buttons |
|----------|------|-------|---------|
| 320px | 40×40px | 1–2 lines | Stack vertically OR shrink font |
| 375px+ | 48×48px | 1–2 lines | Inline (Download / View) |
| 768px+ | 48×48px | 1 line (likely) | Inline, full padding |

**Typography at 320px:**
- Card title: `font-size: 0.875rem` (was 1rem)
- Short desc: `font-size: 0.75rem` (was 0.875rem)
- Metadata badges: `font-size: 0.625rem` (was 0.75rem)

**Action Row at 320px:**
Option A (Recommended): **Stack vertically**
```css
.action-row {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.action-row button {
  width: 100%;
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
}
```

Option B: **Shrink & inline**
```css
.action-row {
  display: flex;
  gap: 0.25rem;
  font-size: 0.75rem;
}

.action-row button {
  flex: 1;
  padding: 0.375rem 0.75rem;
}
```

**Recommendation:** Option A (stacking) feels less cramped. Test both.

**Card Grid at 320px:**
```css
@media (max-width: 640px) {
  .gallery-grid {
    grid-template-columns: 1fr; /* Single column */
    gap: 1rem;
  }
}
```

---

## 5. Error Response Format (Backend)

### Requirement
When download endpoint encounters a missing example path, return 404 with structured JSON so frontend can render helpful error messages.

### Design Spec

**Success Response (200):**
```json
{
  "status": "ok",
  "message": "Download started",
  "filename": "kafka-saga.zip",
  "size": 2048576
}
```

**Not Found (404) — Missing Example:**
```json
{
  "status": "not_found",
  "error": "Example path does not exist in repository",
  "details": {
    "sourceRepo": "operaton/operaton-examples",
    "sourceSha": "abc1234567890abcdef",
    "exampleId": "kafka-saga",
    "expectedPath": "examples/kafka-saga",
    "sourceRepoUrl": "https://github.com/operaton/operaton-examples/tree/abc1234567890abcdef/examples/kafka-saga"
  },
  "suggestion": "The example may have been moved or deleted. Check the repository."
}
```

**Bad Gateway (502) — GitHub Unreachable:**
```json
{
  "status": "service_unavailable",
  "error": "GitHub is temporarily unavailable",
  "suggestion": "Try again in a few moments."
}
```

**Payload Too Large (413):**
```json
{
  "status": "payload_too_large",
  "error": "Example exceeds maximum download size",
  "details": {
    "maxSizeMb": 50,
    "exampleId": "kafka-saga",
    "estimatedSizeMb": 75
  },
  "suggestion": "Contact the example author to reduce size."
}
```

**Implementation Note:**
Frontend catches these responses in `useExampleDownload.ts` and maps to toast messages:
- 404 → "Example not found. Report issue." (with link to sourceRepoUrl)
- 502 → "GitHub unreachable. Try again?"
- 413 → "Example too large (exceeds 50 MB)."

---

## 6. Cache Eviction Policy (Backend)

### Requirement
ZIP cache is bounded by total size (default 512 MB) and uses LRU eviction. Document the exact policy so frontend devs understand caching behavior and can reason about stale content.

### Design Spec

**Cache Key:**
```
{owner}/{repo}/{sha}/{exampleId}.zip
Example: operaton/operaton-examples/abc1234567890abcdef/kafka-saga.zip
```

**Eviction Strategy:**
- **Trigger:** Cache size exceeds `starter.examples.cache.maxSizeMb` (default 512 MB)
- **Policy:** LRU by **access time** (last read or write)
- **Eviction order:** Least recently accessed entries are deleted first until cache is below the limit
- **Granularity:** Entire ZIP file (no partial evictions)

**Staleness Handling:**
- **When Ref Changes:** Manual refresh endpoint updates the pinned SHA for a source. Old SHA's cached ZIPs remain in cache but are now orphaned (no card references them).
- **Natural Cleanup:** LRU eviction removes old SHA entries over time as new examples are downloaded.
- **Explicit Cleanup:** Manual refresh does NOT flush the cache (by design). Operators can manually delete `{java.io.tmpdir}/operaton-starter/examples-cache/` if needed.

**Invariant for Frontend:**
Cached ZIPs are immutable by their SHA key. If a user downloads Example X at SHA abc123, then a manual refresh changes the source to SHA def456, the user's cached ZIP remains valid (same content, same SHA). New users get fresh ZIPs at def456. No conflict.

**Implementation Note:**
Document this in code comments. Frontend devs don't directly interact with cache, but ops/QA need to understand that "old examples still downloadable after refresh until cache evicts them."

---

## 7. Tag Categories & Styling

### Requirement
New tag categories (runtime, integration, concept) fit the existing `Tag` model and `tagColors.ts` system.

### Design Spec

**Existing Categories (via tagColors.ts):**
- BPMN_CONCEPT → `bg-blue-100 text-blue-800`
- TECHNOLOGY → `bg-amber-100 text-amber-800`
- PLATFORM → `bg-green-100 text-green-800`
- STANDARD → `bg-purple-100 text-purple-800`
- Default → `bg-neutral-100 text-neutral-600`

**New Categories to Support:**
- RUNTIME → Already handled by `metadataBadgeClasses()` → `bg-neutral-50 text-neutral-900 border border-neutral-200` (monochrome badge style)
- BUILD_SYSTEM → Same monochrome badge style
- COMPLEXITY → Same monochrome badge style
- INTEGRATION → Should use chipClasses; suggest `bg-amber-100 text-amber-800` (same as TECHNOLOGY)
- CONCEPT → Already mapped to BPMN_CONCEPT (blue chips)

**Update Required in tagColors.ts:**
```typescript
export function tagChipClasses(category: TagCategory | undefined): string {
  switch (category) {
    case 'BPMN_CONCEPT':
      return 'bg-blue-100 text-blue-800'
    case 'CONCEPT':          // New: same as BPMN_CONCEPT
      return 'bg-blue-100 text-blue-800'
    case 'TECHNOLOGY':
    case 'INTEGRATION':      // New: same as TECHNOLOGY
      return 'bg-amber-100 text-amber-800'
    case 'PLATFORM':
      return 'bg-green-100 text-green-800'
    case 'STANDARD':
      return 'bg-purple-100 text-purple-800'
    default:
      return 'bg-neutral-100 text-neutral-600'
  }
}
```

**Card Display Logic:**
- Metadata badges (RUNTIME, BUILD_SYSTEM, COMPLEXITY) render as monochrome badges (smaller, inline)
- Flavor tags (BPMN_CONCEPT, TECHNOLOGY, PLATFORM, STANDARD, INTEGRATION) render as colored chips
- Both appear on compact card AND in expanded detail view

---

## Acceptance Criteria for Design Sign-Off

- [ ] "?" button is placed in top-right of card header; rotates on toggle
- [ ] ⚠️ icon appears right of Download button when `isDownloadable: false`; links to sourceRepoUrl
- [ ] Download button shows "Downloading..." + spinner while in progress
- [ ] Error toast appears below viewport on download failure with Retry button
- [ ] Card layout adapts to 320px: typography shrinks, action buttons stack or compress
- [ ] All color contrasts meet WCAG AA (4.5:1 for text on backgrounds)
- [ ] Keyboard navigation works: Tab through buttons, Enter/Space toggles detail view, Escape collapses
- [ ] Tooltip copy matches spec ("Show details", "Example not found. Report issue →", etc.)
- [ ] Tag categories (RUNTIME, INTEGRATION, etc.) render with correct colors per tagColors.ts

---

## Handoff Checklist

**For Frontend Dev:**
- [ ] Read sections 1–3 (button specs, error icon, progress UI)
- [ ] Read section 4 (mobile layout) and test at 320px
- [ ] Update ExampleGalleryCard.vue with "?" button + ⚠️ icon styling
- [ ] Update DownloadAction.vue with progress spinner + error states
- [ ] Update useExampleDownload.ts to emit `isLoading`, `error`, `errorMessage` states
- [ ] Test with DownloadAction's retry() method
- [ ] Add toast component to GalleryView.vue if not already present

**For Backend Dev:**
- [ ] Implement error responses per section 5 (404, 502, 413 format)
- [ ] Document cache eviction policy (section 6) in loader service code comments
- [ ] Update manifest validation to populate `isDownloadable` field
- [ ] Add unit tests for error paths (missing example, GitHub unreachable, oversized ZIP)

**For QA:**
- [ ] Test at 320px viewport with keyboard nav
- [ ] Test error states: trigger 404 (missing path), 502 (GitHub down), 413 (oversized ZIP)
- [ ] Verify spinner shows during download; toast shows on failure
- [ ] Verify ⚠️ icon click opens correct GitHub URL

---

**Status:** Ready for frontend & backend dev sign-off. One review cycle expected.

**Next:** Create Epic 2 & 3 stories with these specs as acceptance criteria.
