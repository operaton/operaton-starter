# Deferred Work

## Deferred from: code review of 2-10-operaton-banner-in-process-application (2026-06-08)

- `renderSingleTemplate` exposes raw JTE engine errors without sanitization — pre-existing design choice; add error wrapping if a public preview endpoint is wired to it.
- Duplicate `banner.txt` at `jte-sources/` (preview) and `resources/` (ZIP) source paths — no build-time check enforces they stay in sync; consider a resource copy task or single-source approach.
- UC-01 manifest (`MetadataController`) missing `docker-compose.yml`, `EscalationReminderDelegate`, `LeaveRejectionEmailDelegate` entries — pre-existing gap; update manifest when those features land.

## Deferred from: code review of 14-1-bpmn-swimlane-restructuring (2026-06-08)

- No test assertions cover `Task_SendRejectionEmail`, escalation path, or timer boundary in `GenerationEngineTest` — add coverage after swimlane story is confirmed done.
- Non-interrupting timer uses a duration expression — confirm `${managerReviewTimeout}` is always a duration (not a cycle) value; add a comment in the BPMN or DataInitializer to document the expected ISO 8601 format.

## Deferred from: code review of 14-2-uc-01-email-rejection-mailpit (2026-06-08)

- `axllent/mailpit:latest` not version-pinned in docker-compose — pin to a specific version for reproducible dev environments.
- No SMTP credentials placeholder in `application.properties.jte` — add commented-out `spring.mail.username` / `spring.mail.password` lines with guidance for production SMTP relay configuration.

## Deferred from: code review of 4-9-file-content-pane-in-file-structure-preview (2026-06-05)

- No request-level caching of `previewContent` loads in `MetadataController` — each `GET /api/v1/metadata` reads classpath resources for every entry; add caching if latency becomes an issue.
- Integration test assertion in `ApiControllerTest.metadata_returns_preview_content_for_template_entries` is too weak — checks substring presence only; should assert correct content per specific entry.
- `aria-selected` absent on directory `<li>` elements in `FileTreeNode.vue` — ARIA completeness gap in pre-existing tree component.
- Arrow key navigation not implemented on file tree (`FileTreeNode.vue`) — pre-existing gap; story only required Tab+Enter per FR22; full ARIA tree keyboard pattern deferred.
- `aria-expanded` always `true` for directories regardless of children in `FileTreeNode.vue` — pre-existing issue.
- Large `previewContent` values have no server-side size cap — `overflow-auto` present in `FileContentPane.vue` but long lines can still cause layout issues; add max-h or truncation in a future story.
- Copy button rapid-click timeout not debounced in `FileContentPane.vue` — minor UX polish.
- Long filenames truncated with no tooltip in `FileContentPane.vue` — add `title` attribute for full name on hover.

## Deferred from: quick dev review of spec-leave-request-vacation-balances-and-detailed-use-case-readmes (2026-06-06)

- UC-02/03/04 switched from SQL-based identity seeding to `DataInitializer.java`, but generation tests still do not assert those generated archives include the initializer or preserve runnable seeded demo identities; add focused coverage in a later template-validation story.
