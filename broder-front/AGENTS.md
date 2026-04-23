# broder-front — Agent Notes

> Compact reference for the React + TypeScript + Vite frontend. The existing `AGENTS.md` was for the Java backend — this repo is the frontend only.

---

## Stack & Tooling

- **React 19** + **TypeScript 6** + **Vite 8**
- **Package manager:** `pnpm` (lockfile: `pnpm-lock.yaml`)
- **ESLint:** flat config (`eslint.config.js`) — `@eslint/js`, `typescript-eslint`, `react-hooks`, `react-refresh`
- **No test runner configured** yet (`src/test/` does not exist)

---

## Quick Commands

```bash
# Install dependencies
pnpm install

# Dev server (Vite default port 5173)
pnpm dev

# Type-check + build (order matters: tsc first)
pnpm build

# Lint only
pnpm lint

# Preview production build
pnpm preview
```

---

## TypeScript Configuration Quirks

The root `tsconfig.json` is a **solution file** with project references:
- `tsconfig.app.json` → application code (`src/`)
- `tsconfig.node.json` → Vite config (`vite.config.ts`)

Both use `bundler` module resolution and share these strict flags that will **fail the build** if violated:

| Flag | Effect |
|------|--------|
| `verbatimModuleSyntax: true` | Type-only imports **must** use `import type { Foo }`. Plain `import { Foo }` where `Foo` is only a type will error. |
| `erasableSyntaxOnly: true` | Bans runtime TypeScript syntax: **no `enum`**, **no `namespace`**, **no parameter properties**. Use plain objects / `const` maps instead. |
| `noUnusedLocals: true` | Unused variables cause build failure. |
| `noUnusedParameters: true` | Unused function parameters cause build failure. Prefix with `_` if intentional. |
| `allowImportingTsExtensions: true` | Import paths include `.tsx` / `.ts` extensions (e.g., `import App from './App.tsx'`). |
| `noEmit: true` | `tsc` only type-checks; Vite handles compilation. |

---

## Conventions

- **Imports:** Use `.tsx` / `.ts` extensions in import paths. Use `import type` for type-only imports.
- **No enums:** Because of `erasableSyntaxOnly`, define constants as `as const` objects instead.
- **ESLint:** Flat config. Add new rules in `eslint.config.js`; do not create `.eslintrc` files.
- **Styling:** Plain CSS (`src/index.css`, `src/App.css`). No CSS-in-JS or Tailwind configured.
- **Assets:** Static assets go in `src/assets/` or `public/` (the latter for files referenced directly from `index.html`).

---

## Notes

- This frontend is designed to consume the **broder backend API** (Quarkus, typically `http://localhost:8080`). The backend repo is separate; CORS there is already configured for `http://localhost:3000` (React dev) — update the backend if the Vite dev server runs on a different origin.
- `pnpm build` runs `tsc -b` first. If type-check fails, Vite build does not run.
