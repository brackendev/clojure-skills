# clojure-skills

Clojure and ClojureDart development skills packaged as an [APM](https://github.com/microsoft/apm) plugin. One install deploys all thirteen skills to every runtime APM supports: Claude Code, Codex, OpenCode, Cursor, Copilot, Gemini, and Windsurf.

Skills follow the [Agent Skills](https://agentskills.io) open standard. Five auto-trigger from conversation context (`clojure`, `clojuredart`, `clojure-lenses`, `clojuredart-lenses`, `cljd-nav`); the rest appear as slash commands.

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

- [Clojure CLI](https://clojure.org/guides/install_clojure) and [clj-kondo](https://github.com/clj-kondo/clj-kondo) for any Clojure skill.
- [Flutter SDK](https://docs.flutter.dev/get-started/install) and [ClojureDart](https://github.com/Tensegritics/ClojureDart) for any ClojureDart skill.
- The `clj-check` and `cljd-check` dry steps require a [dry4clj](https://github.com/unclebob/dry4clj) `:dry4clj` alias in `deps.edn`.

## Skills

### Clojure: scaffolding and quality

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

### ClojureDart: scaffolding, navigation, quality, testing, upgrades

#### `/cljd-new <project-name>`

Scaffold a new ClojureDart Flutter project, including clj-kondo lint setup.

```bash
/cljd-new my-app
```

#### `/cljd-check [lint|format|compile|dry]`

Run the ClojureDart quality pipeline. Defaults to lint, format, compile, dry. The dry step scans `.clj`, `.cljc`, and `.cljs` files; `.cljd` coverage requires the upstream dry4clj extension (see TODO).

```bash
/cljd-check
/cljd-check compile
```

#### `/cljd-test [unit|widget|all]`

Scaffold and run ClojureDart tests with `cljd.test`.

```bash
/cljd-test
/cljd-test widget
```

#### `/cljd-upgrade`

Upgrade ClojureDart to the latest version. Updates the `tensegritics/clojuredart` dependency SHA in `deps.edn`.

```bash
/cljd-upgrade
```

#### `/cljd-smells-review [scope or options...]` (placeholder)

Reserves the command name for a future ClojureDart-specific smells review. Currently prints a "not yet implemented" notice and exits. See [TODO.md](TODO.md).

### Auto-triggered

These skills activate from conversation context. They cannot be invoked directly.

| Skill | Triggers |
|-------|----------|
| **clojure** | `.clj` files, `deps.edn` projects, `project.clj`, `build.clj`, `clojure.test`, REPL usage, mention of Clojure. Covers idiomatic style, naming conventions, threading macros, collection idioms, state management, and common anti-patterns. |
| **clojuredart** | `.cljd` files, `deps.edn` with `tensegritics/clojuredart`, `cljd-out/` directories, Flutter integration, ClojureDart REPL usage. Covers syntax, Dart interop, widget macros, project structure, compilation, and REPL-driven development. |
| **clojure-lenses** | Auto-triggers alongside the [code-lenses](https://github.com/brackendev/code-lenses) plugin in Clojure work. Translates grug, APOSD, Tidy First, Parse Don't Validate, Honest Code, and Legacy Code reviews into idiomatic Clojure. |
| **clojuredart-lenses** | Auto-triggers alongside the [code-lenses](https://github.com/brackendev/code-lenses) plugin in ClojureDart work. Translates the same design lenses into ClojureDart and Flutter patterns. |
| **cljd-nav** | ClojureDart navigation discussions: named routes, `go_router`, the `Navigator` API, tab navigation, and deep linking in Flutter. |

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

MIT. See [LICENSE](LICENSE).
