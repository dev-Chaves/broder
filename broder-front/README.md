# Broder Frontend

React + TypeScript + Vite frontend for the Broder alerting platform.

## Stack

- React 19
- TypeScript 6
- Vite 8
- React Router 7
- dnd-kit (drag-and-drop)
- Lucide React (icons)

## Development

```bash
# Install dependencies
pnpm install

# Start dev server (port 3000)
pnpm dev
```

The dev server proxies `/api` requests to `http://localhost:8080` (Broder backend).

## Build

```bash
pnpm build
```

Outputs static files to `dist/`.

## Docker

```bash
docker build -t broder-frontend .
```

Served via nginx on port 80, with `/api` proxied to the backend.
