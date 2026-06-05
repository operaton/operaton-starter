# Deferred Work

## Deferred from: code review of 4-9-file-content-pane-in-file-structure-preview (2026-06-05)

- No request-level caching of `previewContent` loads in `MetadataController` — each `GET /api/v1/metadata` reads classpath resources for every entry; add caching if latency becomes an issue.
- Integration test assertion in `ApiControllerTest.metadata_returns_preview_content_for_template_entries` is too weak — checks substring presence only; should assert correct content per specific entry.
- `aria-selected` absent on directory `<li>` elements in `FileTreeNode.vue` — ARIA completeness gap in pre-existing tree component.
- Arrow key navigation not implemented on file tree (`FileTreeNode.vue`) — pre-existing gap; story only required Tab+Enter per FR22; full ARIA tree keyboard pattern deferred.
- `aria-expanded` always `true` for directories regardless of children in `FileTreeNode.vue` — pre-existing issue.
- Large `previewContent` values have no server-side size cap — `overflow-auto` present in `FileContentPane.vue` but long lines can still cause layout issues; add max-h or truncation in a future story.
- Copy button rapid-click timeout not debounced in `FileContentPane.vue` — minor UX polish.
- Long filenames truncated with no tooltip in `FileContentPane.vue` — add `title` attribute for full name on hover.
