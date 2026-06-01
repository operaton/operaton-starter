# starter-mcp

## Role

MCP (Model Context Protocol) npm package for operaton-starter. Exposes a `generate_project` tool that AI assistants (Claude, GitHub Copilot, Cursor) can call during development conversations to scaffold Operaton process projects directly.

Published as `operaton-starter-mcp` on npm.

## Prerequisites

- Node.js 22+
- npm 10+

## Build in Isolation

```bash
cd starter-mcp
npm ci
npm run build
```

## Run / Use

`starter-mcp` is not a standalone process — it is registered in an AI assistant's MCP configuration. The assistant spawns it via `npx` when needed.

By default the package calls `https://start.operaton.org`. To point at a self-hosted instance, set `OPERATON_STARTER_URL`.

## Example

**Claude Desktop** (`~/Library/Application Support/Claude/claude_desktop_config.json`):

```json
{
  "mcpServers": {
    "operaton-starter": {
      "command": "npx",
      "args": ["-y", "operaton-starter-mcp"],
      "env": {
        "OPERATON_STARTER_URL": "http://localhost:8080"
      }
    }
  }
}
```

**VS Code / GitHub Copilot** (`.vscode/mcp.json`):

```json
{
  "servers": {
    "operaton-starter": {
      "command": "npx",
      "args": ["-y", "operaton-starter-mcp"],
      "env": {
        "OPERATON_STARTER_URL": "http://localhost:8080"
      }
    }
  }
}
```

Omit `OPERATON_STARTER_URL` to use the public instance at `https://start.operaton.org`.
