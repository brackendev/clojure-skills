# Clojure Skills for Codex

Clojure and ClojureDart development skills for Codex: style guides, project scaffolding, quality checks, and design philosophy translations.

This package lives in `./codex/`.

## What It Provides

Two skill bundles:

- **clojure** -- Idiomatic style guide, project scaffolding with deps.edn, and quality checks with clj-kondo, cljfmt, and test runner.
- **clojuredart** -- ClojureDart syntax, Dart interop patterns, Flutter project scaffolding, quality checks, and dependency upgrades.

These skills are used directly in prompts in Codex.

## Setup

To install these skills in Codex, run this in a Codex thread:

```text
$skill-installer https://github.com/brackendev/clojure-skills
```

Then restart Codex to pick up the newly installed skills.

This repo also includes repo-scoped Codex package metadata:

| Path | Purpose |
|------|---------|
| `./codex/clojure` | Codex package root for Clojure |
| `./codex/clojure/.codex-plugin/plugin.json` | Codex plugin manifest |
| `./codex/clojuredart` | Codex package root for ClojureDart |
| `./codex/clojuredart/.codex-plugin/plugin.json` | Codex plugin manifest |
| `./.agents/plugins/marketplace.json` | Repo-level Codex marketplace entry |

The bundled skills live under the `skills/` directories for the `clojure` and `clojuredart` packages.

## How To Use It

After the skills are installed, use the skill names directly in your prompt when you want Codex to apply them.

You can also type `@` to select one of the installed skills explicitly.

Examples:

```text
Use clj-new to scaffold a Clojure project called my-app.
Use clj-check to lint and format this project.
Use cljd-new to scaffold a ClojureDart Flutter project.
Use cljd-check to run the quality pipeline.
Use cljd-upgrade to update the ClojureDart dependency.
Use cljd-test to run the test suite.
Use cljd-test widget to run widget tests.
```

## Bundled Skills

See the [root README](../README.md) for the full list of skills and auto-triggered behaviors.
