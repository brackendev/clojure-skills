# clojure-skills

Clojure development skills packaged as an [APM](https://github.com/microsoft/apm) plugin. One install deploys the full set to every runtime APM supports: Claude Code, Codex, OpenCode, Cursor, Copilot, Gemini, and Windsurf.

Skills follow the [Agent Skills](https://agentskills.io) open standard. Two auto-trigger from conversation context (`clojure`, `clojure-lenses`); the rest appear as slash commands.

## Companion packages

This package covers idiomatic Clojure on the JVM. Install alongside it as needed:

| Package | Focus |
|---------|-------|
| [clojure-skills](https://github.com/brackendev/clojure-skills) (this package) | Idiomatic Clojure style, scaffolding, quality checks, and code review. |
| [biff-skills](https://github.com/brackendev/biff-skills) | [Biff](https://biffweb.com/) web framework: scaffolding, framework conventions, deployment. Designed to layer on top of this package. |
| [clojuredart-skills](https://github.com/brackendev/clojuredart-skills) | ClojureDart / Flutter equivalents for the Clojure toolkit. |

## Install

Install [APM](https://github.com/microsoft/apm) first if you don't already have it. Then, in a project:

```bash
apm install brackendev/clojure-skills --target all
```

Globally for your user account:

```bash
apm install brackendev/clojure-skills -g --target all
```

Update later with `apm update [-g]`. Remove with `apm uninstall brackendev/clojure-skills [-g]`. A local filesystem path can replace the shorthand at either scope.

## Requirements

- [Clojure CLI](https://clojure.org/guides/install_clojure) and [clj-kondo](https://github.com/clj-kondo/clj-kondo) for any skill in this package.
- The `clj-check` dry step requires a [dry4clj](https://github.com/unclebob/dry4clj) `:dry4clj` alias in `deps.edn`.

## Skills

### Scaffolding and quality

#### `/clj-new <project-name>`

Scaffold a new Clojure project with `deps.edn`.

```bash
/clj-new my-service
```

#### `/clj-check [lint|format|test|dry]`

Run the Clojure quality pipeline. Defaults to the full sequence (lint, format, test, dry). Each step is also addressable on its own.

```bash
/clj-check
/clj-check lint
/clj-check dry
```

#### `/clj-smells-review [scope or options...]`

Review Clojure code against the [clj-smells catalog](https://github.com/nufuturo-ufcg/clj-smells-catalog), pairing `clj-kondo` static analysis with LLM-assisted detection. Reports findings with `DEFECT`, `SMELL`, and `HINT` severity tiers plus a smell density verdict. Falls back to LLM-only review when `clj-kondo` is not on `PATH`.

```bash
/clj-smells-review
/clj-smells-review src/auth.clj
/clj-smells-review all
/clj-smells-review fix
```

### Auto-triggered

These skills activate from conversation context. They cannot be invoked directly.

| Skill | Triggers |
|-------|----------|
| **clojure** | `.clj` files, `deps.edn` projects, `project.clj`, `build.clj`, `clojure.test`, REPL usage, mention of Clojure. Covers idiomatic style, naming conventions, threading macros, collection idioms, state management, and common anti-patterns. |
| **clojure-lenses** | Auto-triggers alongside the [code-lenses](https://github.com/brackendev/code-lenses) plugin in Clojure work. Translates grug, APOSD, Tidy First, Parse Don't Validate, Honest Code, and Legacy Code reviews into idiomatic Clojure. |

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

MIT. See [LICENSE](LICENSE).
