---
name: Arc42 Documentation Standard
description: Architecture documentation must use the arc42 template, stay current, and be handed to the technical writer agent
type: feedback
---

Always document system architecture using the **arc42 template**.

**Why:** User's explicit requirement for the bmad-create-architecture workflow.

**How to apply:**
- When producing architecture output (via bmad-create-architecture or any architecture work), structure content according to arc42 sections (Context, Constraints, Solution Strategy, Building Block View, Runtime View, Deployment View, etc.)
- **Always render diagrams using Mermaid** — never use ASCII art or text-only descriptions for structural diagrams
- Store arc42 documentation in `./docs` (project root `/docs` folder)
- Keep documentation up to date as architecture decisions evolve
- After architecture work is complete or a significant section is done, hand over to the **technical writer agent** (`bmad-agent-tech-writer`) to write concise, polished documentation
