# Clojure Skills

Clojure and ClojureDart development skills for AI coding agents. The same skills are packaged for Claude Code and Codex using the [Agent Skills](https://agentskills.io) open standard.

## What It Is

Clojure Skills provides development guidance through two plugins:

- `clojure`: idiomatic style, project scaffolding, and quality checks for Clojure ([bbatsov/clojure-style-guide](https://github.com/bbatsov/clojure-style-guide))
- `clojuredart`: syntax, Dart interop, project scaffolding, and quality checks for [ClojureDart](https://github.com/Tensegritics/ClojureDart)

Each plugin bundles two types of skills:

- Invocable skills for scaffolding and running quality pipelines
- Auto-triggered skills that provide language-specific guidance as the agent works

## Platforms

| Platform | Package Path | Marketplace Metadata | Guide |
|----------|--------------|----------------------|-------|
| Claude Code | `./claude/` | `./.claude-plugin/marketplace.json` | [Claude README](./claude/README.md) |
| Codex | `./codex/` | `./.agents/plugins/marketplace.json` | [Codex README](./codex/README.md) |

## Bundled Skills

### Clojure

| Skill | Purpose |
|-------|---------|
| `clj-new` | Scaffold a new Clojure project with deps.edn, source layout, test runner, tools.build, and cljfmt |
| `clj-check` | Run the quality pipeline: lint with clj-kondo, format with cljfmt, run tests with cognitect test-runner |
| `clojure` | Auto-triggered style guide for `.clj` files and `deps.edn` projects |

### ClojureDart

| Skill | Purpose |
|-------|---------|
| `cljd-new` | Scaffold a new ClojureDart Flutter project with deps.edn, entry point, and formatting rules |
| `cljd-check` | Run the quality pipeline: lint with clj-kondo, format with cljfmt, compile with ClojureDart |
| `cljd-upgrade` | Upgrade the ClojureDart dependency to the latest version |
| `cljd-test` | Scaffold and run ClojureDart tests: unit tests, widget tests, integration tests, and tag-based filtering |
| `cljd-nav` | Auto-triggered navigation patterns: named routes, go_router, Navigator API, tab navigation, and deep linking |
| `clojuredart` | Auto-triggered syntax, Dart interop patterns, async, platform channels, and project structure guidance for `.cljd` files |
| `clojuredart-lenses` | Auto-triggered translation of code-lenses design philosophies to idiomatic ClojureDart and Flutter patterns |

## How To Use It

Choose the platform guide that matches your agent:

- [Claude README](./claude/README.md)
- [Codex README](./codex/README.md)

Both packages expose the same skills. The main difference is how they are invoked:

- Claude Code uses installed plugin commands such as `/clojure:clj-check`
- Codex uses the packaged skills directly in prompts such as `Use clj-check to run the quality pipeline.`

## Repository Layout

| Path | Contents |
|------|----------|
| `claude/clojure/` | Claude package: manifest and skills for Clojure |
| `claude/clojuredart/` | Claude package: manifest and skills for ClojureDart |
| `codex/clojure/` | Codex package: manifest and skills for Clojure |
| `codex/clojuredart/` | Codex package: manifest and skills for ClojureDart |
| `.claude-plugin/marketplace.json` | Repo-level Claude marketplace entry |
| `.agents/plugins/marketplace.json` | Repo-level Codex marketplace entry |

## Notes

- Clojure plugins require [Clojure CLI](https://clojure.org/guides/install_clojure) (tools.deps) and [clj-kondo](https://github.com/clj-kondo/clj-kondo).
- ClojureDart plugins additionally require [Flutter SDK](https://docs.flutter.dev/get-started/install) and [ClojureDart](https://github.com/Tensegritics/ClojureDart).
- [brackendev/code-lenses](https://github.com/brackendev/code-lenses) provides design philosophy skills (grug, APOSD, Tidy First, Parse Don't Validate, Honest Code, Legacy Code). When installed alongside this package, the **clojure-lenses** and **clojuredart-lenses** skills auto-trigger to translate language-agnostic design advice into idiomatic Clojure and ClojureDart patterns.
- [bhauman/clojure-mcp](https://github.com/bhauman/clojure-mcp) provides an MCP server for REPL-driven Clojure development with REPL evaluation, namespace management, and file operations.
- The skill content follows the [Agent Skills](https://agentskills.io) format, so other compatible tools can reuse the skill directories if they integrate them separately.
