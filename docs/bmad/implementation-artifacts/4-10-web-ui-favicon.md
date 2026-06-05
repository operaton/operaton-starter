---
baseline_commit: 48f7c1c
---

# Story 4.10: Web UI Favicon

## Status
done

## Story

As a **developer using the Operaton Starter web UI**,
I want browser tabs and bookmarks to display the Operaton logo mark,
So that the tool is visually identifiable alongside other open tabs and feels part of the Operaton product family.

## Acceptance Criteria

1. **Given** a browser opens `start.operaton.org` (or any page served by the application) **When** the page loads **Then** the browser tab displays the Operaton favicon (logo mark)

2. **Given** a developer bookmarks any page **When** they view their bookmarks **Then** the bookmark shows the Operaton favicon

3. **Given** a request to `/favicon.ico` is made **When** the server handles it **Then** it returns HTTP 200 with the favicon file; no 404 is emitted (FR60)

4. **Given** the Vue app build **When** `npm run build` executes **Then** `favicon.ico` is included in the built static output under `starter-server/src/main/resources/static/`

5. **Given** the favicon **When** inspected **Then** it is derived from the Operaton logo mark (consistent with the Operaton brand)

## Tasks/Subtasks

- [x] Task 1: Add favicon.ico to starter-web/public/
  - [x] 1.1: Place `favicon.ico` derived from the Operaton logo in `starter-web/public/`
  - [x] 1.2: Verify Vite copies `public/` assets to the build output (standard Vite behaviour)

- [x] Task 2: Ensure favicon is served by Spring Boot
  - [x] 2.1: Confirm `starter-server/src/main/resources/static/favicon.ico` is present after `mvn verify` (Vite build copies to this directory per `vite.config.ts` `outDir`)
  - [x] 2.2: Verify Spring Boot serves `/favicon.ico` with HTTP 200 (default static resource serving)

- [x] Task 3: Reference favicon in HTML
  - [x] 3.1: Add `<link rel="icon" href="/favicon.ico">` to `starter-web/index.html`

## Dev Notes

- Vite's `public/` directory is copied verbatim to the build output (`outDir`); no import or reference needed in the build config — placing the file in `public/` is sufficient
- Spring Boot auto-serves anything under `src/main/resources/static/` at the root path; no `@GetMapping` needed
- If the Operaton SVG logo is available, an `.ico` can be generated at 16x16, 32x32, and 48x48 sizes using `magick convert` or a similar tool

## Change Log

- 2026-06-03: Story created to document FR60 implementation — favicon.ico present in starter-server/src/main/resources/static/ and starter-web/public/
