# Clojure Skills for Codex

Clojure and ClojureDart development skills for Codex: style guides, project scaffolding, quality checks, and design philosophy translations.

This package lives in `./codex/`.

## What It Provides

Two plugins:

- **clojure** -- Idiomatic style guide, project scaffolding with deps.edn, and quality checks with clj-kondo, cljfmt, and test runner.
- **clojuredart** -- ClojureDart syntax, Dart interop patterns, Flutter project scaffolding, quality checks, and dependency upgrades.

These plugins are used through skills referenced in prompts in Codex.

## Setup

This repo includes repo-scoped marketplace metadata for Codex:

| Path | Purpose |
|------|---------|
| `./codex/clojure` | Codex package root for Clojure |
| `./codex/clojure/.codex-plugin/plugin.json` | Codex plugin manifest |
| `./codex/clojuredart` | Codex package root for ClojureDart |
| `./codex/clojuredart/.codex-plugin/plugin.json` | Codex plugin manifest |
| `./.agents/plugins/marketplace.json` | Repo-level Codex marketplace entry |

To install these plugins in Codex:

1. Open the repository root in Codex, not the `./codex/` subdirectory. Codex needs the repo root so it can see `./.agents/plugins/marketplace.json`.
2. Restart Codex if this repo was already open before the marketplace file or plugin files were added or changed.
3. Open the plugin directory:
   - In the Codex app, open `Plugins`.
   - In Codex CLI, run `codex` and enter `/plugins`.
4. Find the repo marketplace entry and install `Clojure` or `ClojureDart`.
5. Start a new thread and ask Codex to use one of the bundled skills.

The marketplace entry points Codex to `./codex/clojure` and `./codex/clojuredart`, and the bundled skills live under their respective `skills/` directories.

## How To Use It

After the plugins are installed, use the skill names directly in your prompt when you want Codex to apply them.

You can also type `@` to select the plugin or one of its bundled skills explicitly.

Examples:

```text
Use clj-new to scaffold a Clojure project called my-app.
Use clj-check to lint and format this project.
Use cljd-new to scaffold a ClojureDart Flutter project.
Use cljd-check to run the quality pipeline.
Use cljd-upgrade to update the ClojureDart dependency.
```

## Bundled Skills

See the [root README](../README.md) for the full list of skills and auto-triggered behaviors.
