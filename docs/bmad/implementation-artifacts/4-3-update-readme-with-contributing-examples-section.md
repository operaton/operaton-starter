---
baseline_commit:
---

# Story 4-3: Update README with Contributing Examples Section

## Status
ready-for-dev

## Story

As a **new contributor exploring operaton-starter**,
I want **the main README to mention examples and how to contribute them**,
So that **I discover the feature and contributing pathway early**.

## Acceptance Criteria

**Given** the operaton-starter README already documents the project  
**When** I read the README  
**Then** I find a new section titled **"Contributing Examples"** or **"Share Your Examples"** containing:
1. **Brief intro** (1–2 sentences): "The Examples Gallery showcases real-world Operaton patterns. You can contribute your own examples by publishing a repository with a manifest file."
2. **Quick start link** (inline or button): "See [the examples repository format guide](./docs/examples-repository-format.md)" or URL to published docs
3. **Example structure callout** (3–4 lines):
   ```
   All you need:
   - A GitHub repository with example code
   - A `.operaton-starter.yml` manifest at the root
   - Let us know the repo URL
   ```
4. **Contact/Process** (1–2 lines): "To register your repository, [open an issue](https://github.com/operaton/operaton-starter/issues) with the repo URL and a brief description, or contact us at [email/Slack channel]."
**Placement:**
- After "Getting Started" or "Features" section
- Before "Development" or "Contributing Code" (if those sections exist)
- Keep README scannable: max 100 words for this section
**Cross-link confirmation:**
- README links to `docs/examples-repository-format.md` ✓
- `docs/examples-repository-format.md` mentions the README (optional back-link) ✓
**Review:** README edits pass project style guide; links are valid; tone matches existing README
---

## Tasks/Subtasks

- [ ] Implementation tasks to be defined based on story requirements

## Dev Notes

- See epic-level documentation in epics-examples-gallery-v1.md for full context
- All stories should follow the patterns established in Examples Gallery v1 PRD

## Dev Agent Record

### Completion Notes
(To be filled during implementation)

### File List
(To be filled during implementation)

### Change Log
(To be filled during implementation)
