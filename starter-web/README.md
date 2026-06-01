# starter-web

## Role

Vue 3 single-page application for operaton-starter. Provides the browser-based project configuration UI. Built with Vite, TypeScript, and Tailwind CSS.

In production, the compiled static assets are bundled into `starter-server` and served from the same origin. In development, Vite's dev server proxies `/api/**` calls to the backend.

## Prerequisites

- Node.js 22+
- npm 10+

## Build in Isolation

```bash
cd starter-web
npm ci
npm run build
```

Output goes to `starter-web/dist/`.

## Run Dev Server

Start the backend first (see [starter-server/README.md](../starter-server/README.md) or use docker compose):

```bash
# Option A — run the backend directly
mvn spring-boot:run -pl starter-server -am -f ../pom.xml

# Option B — use docker compose (requires a prior mvn verify + docker build)
docker compose -f ../docker-compose.dev.yml up
```

Then start the Vite dev server:

```bash
cd starter-web
npm run dev
```

The dev server starts at `http://localhost:5173`. API requests to `/api/**` are proxied to `http://localhost:8080` (configured in `vite.config.ts`).

## Example

```bash
npm run test:unit   # Vitest unit tests
npm run lint        # ESLint
npm run type-check  # TypeScript type checking
```
