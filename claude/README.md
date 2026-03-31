# Clojure Skills for Claude Code

Clojure and ClojureDart development skills for Claude Code: style guides, project scaffolding, quality checks, and design philosophy translations.

This package lives in `./claude/`.

## What It Provides

Two plugins:

- **clojure** -- Idiomatic style guide, project scaffolding with deps.edn, and quality checks with clj-kondo, cljfmt, and test runner.
- **clojuredart** -- ClojureDart syntax, Dart interop patterns, Flutter project scaffolding, quality checks, and dependency upgrades.

These plugins are installed and invoked as Claude plugins with slash commands.

## Install

This repo includes the Claude packages and marketplace metadata:

| Path | Purpose |
|------|---------|
| `./claude/clojure` | Claude package root for Clojure |
| `./claude/clojure/.claude-plugin/plugin.json` | Claude plugin manifest |
| `./claude/clojuredart` | Claude package root for ClojureDart |
| `./claude/clojuredart/.claude-plugin/plugin.json` | Claude plugin manifest |
| `./.claude-plugin/marketplace.json` | Repo-level Claude marketplace entry |

Add the marketplace:

```bash
/plugin marketplace add brackendev/clojure-skills
```

Install the plugins:

```bash
/plugin install clojure@clojure-skills
/plugin install clojuredart@clojure-skills
```

Uninstall:

```bash
/plugin uninstall clojure@clojure-skills
/plugin uninstall clojuredart@clojure-skills
/plugin marketplace remove brackendev/clojure-skills
```

## How To Use It

Invoke skills through slash commands.

### Clojure

```bash
/clojure:clj-new my-app
/clojure:clj-check
/clojure:clj-check lint
```

### ClojureDart

```bash
/clojuredart:cljd-new my-app
/clojuredart:cljd-check
/clojuredart:cljd-upgrade
```

## Bundled Skills

See the [root README](../README.md) for the full list of skills and auto-triggered behaviors.
