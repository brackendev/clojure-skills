# clojure-skills

Clojure development skills packaged as an [APM](https://github.com/microsoft/apm) plugin. One install deploys the full set to every runtime APM supports: Claude Code, Codex, OpenCode, Cursor, Copilot, Gemini, Windsurf, and Kiro.

Skills follow the [Agent Skills](https://agentskills.io) open standard. Two auto-trigger from conversation context (`clojure`, `clojure-lenses`); the rest appear as slash commands.

The `clojure` skill is the host-neutral baseline for the Clojure family. It triggers across `.clj`, `.cljs`, `.cljc`, and `.cljd`. Host-specific guidance (Java interop, refs / agents / STM, ClojureDart's `cljd.flutter` directives, Biff conventions) lives in companion packages so this skill stays applicable everywhere.

## Companion packages

Six sibling APM packages. Install whichever match your project. Each carries the runtime-specific or framework-specific delta on top of this host-neutral baseline.

| Package | Focus | Layers on |
|---------|-------|-----------|
| [clojure-skills](https://github.com/brackendev/clojure-skills) (this package) | Host-neutral [Clojure](https://clojure.org/) family baseline: style, naming, threading, collections, atoms, dispatch, formatting, namespaces, and testing. Triggers on `.clj`, `.cljs`, `.cljc`, `.cljd`. | — |
| [clojure-jvm-skills](https://github.com/brackendev/clojure-jvm-skills) | [Clojure](https://clojure.org/) on the JVM: Java interop, refs / agents / STM, `with-open`, JVM-typed exceptions, `alter-var-root`, and the Clojure CLI / `tools.build` / `clj-kondo` / `cljfmt` / `test-runner` / nREPL workflow. | `clojure-skills` |
| [clojurescript-skills](https://github.com/brackendev/clojurescript-skills) | [ClojureScript](https://clojurescript.org/) style and tooling: JavaScript interop, externs inference, macro stage separation, `catch :default`, JS-flavored numbers and truthiness, and the `cljs.main` workflow. Triggers on `.cljs`, `.cljc` compiled to JS, `shadow-cljs.edn`, `figwheel-main.edn`. | `clojure-skills` |
| [biff-skills](https://github.com/brackendev/biff-skills) | [Biff](https://biffweb.com/) web framework on the JVM: scaffolding, conventions, deployment. | `clojure-skills` + `clojure-jvm-skills` |
| [fulcro-skills](https://github.com/brackendev/fulcro-skills) | [Fulcro](https://github.com/fulcrologic/fulcro) full-stack framework: `defsc` components, idents and the normalized client database, mutations, `df/load!`, dynamic routing, forms, UI state machines, Fulcro Inspect, and the Pathom 3 server. Triggers on `com.fulcrologic.fulcro.*`, `com.fulcrologic.rad.*`, `com.wsscode.pathom3.*`, `defsc`, `defmutation`, `defrouter`, `df/load!`, and ident vectors. | `clojure-skills` + `clojurescript-skills` + `clojure-jvm-skills` |
| [clojuredart-skills](https://github.com/brackendev/clojuredart-skills) | [ClojureDart](https://github.com/Tensegritics/ClojureDart) on [Flutter](https://flutter.dev/): Dart interop, type hints, `cljd.flutter` directives, async, FFI, REPL, and the Flutter project workflow. Triggers on `.cljd`, `cljd.flutter`. | `clojure-skills` |

## Install

Install [APM](https://github.com/microsoft/apm) first if you don't already have it. Then, in a project:

```bash
apm install brackendev/clojure-skills --target all
```

Globally for your user account:

```bash
apm install brackendev/clojure-skills -g --target all
```

For JVM Clojure work, install `clojure-jvm-skills` alongside:

```bash
apm install brackendev/clojure-jvm-skills -g --target all
```

Update later with `apm update [-g]`. Remove with `apm uninstall brackendev/clojure-skills [-g]`. A local filesystem path can replace the shorthand at either scope.

## Requirements

- A Clojure dialect runtime for whichever skill you exercise: the [Clojure CLI](https://clojure.org/guides/install_clojure) and Java 17 or higher for JVM Clojure, [ClojureDart](https://github.com/Tensegritics/ClojureDart) for `.cljd` work, [shadow-cljs](https://github.com/thheller/shadow-cljs) or similar for ClojureScript.
- [clj-kondo](https://github.com/clj-kondo/clj-kondo) for the lint steps in the user-invoked skills below.
- The `clj-fix` dry step requires a [dry4clj](https://github.com/brackendev/dry4clj) `:dry4clj` alias in `deps.edn`.

## Command guide

A quick guide to every slash command. The detailed entries under [Skills](#skills) cover arguments and examples.

| Command | Use it when | What it does |
|---------|-------------------|--------------|
| `/clj-new` | Starting a new Clojure project | Scaffolds a project with `deps.edn` |
| `/clj-fix` | Lint, format, or tests need attention | Runs the quality pipeline, rewriting files in the format step with `cljfmt fix` |
| `/clj-smells-fix` | Clojure code has smells to address | Applies the safe mechanical and DEFECT-band fixes and reports the rest |

## Skills

### Scaffolding and quality

#### `/clj-new <project-name>`

Scaffold a new Clojure project with `deps.edn`.

```bash
/clj-new my-service
```

#### `/clj-fix [lint|format|test|dry] [--report]`

Run the Clojure quality pipeline. Defaults to the full sequence (lint, format, test, dry). The `format` step rewrites files with `cljfmt fix`; the other three steps are pure-read. Pass `--report` to swap the format step for `cljfmt check`, which previews diffs without writing. Each step is also addressable on its own.

```bash
/clj-fix
/clj-fix lint
/clj-fix --report
/clj-fix format --report
```

#### `/clj-smells-fix [path|all] [--report]`

Fix Clojure code against the [clj-smells catalog](https://github.com/nufuturo-ufcg/clj-smells-catalog), pairing `clj-kondo` static analysis with LLM-assisted detection. Auto-applies Stage 1 mechanical findings (`:refer :all` expansion, `:use` rewrite, redundant `do`, nested `let`, unused requires and bindings) and the safe band of Stage 2 `DEFECT`-tier findings (macro double-evaluation, unwrapped resource handles, blocking inside `go`, load-time side effects in `def` bodies). `SMELL` and `HINT` findings, and `DEFECT` findings that require cross-file changes, are reported as suggestions. Pass `--report` to disable all writes and receive the same findings as suggestions. Falls back to LLM-only review when `clj-kondo` is not on `PATH`.

```bash
/clj-smells-fix
/clj-smells-fix src/auth.clj
/clj-smells-fix all
/clj-smells-fix --report
```

### Auto-triggered

These skills activate from conversation context. They cannot be invoked directly.

| Skill | Triggers |
|-------|----------|
| **clojure** | `.clj`, `.cljs`, `.cljc`, `.cljd` files; `deps.edn` projects; `shadow-cljs.edn`; `bb.edn`; `project.clj`; `build.clj`; `clojure.test`, `cljs.test`, `cljd.test`; `clojure.core` forms; REPL usage; or any mention of Clojure, ClojureScript, ClojureDart, or a Clojure dialect. Covers idiomatic style, naming, threading macros, collection idioms, atom-based state, dispatch, formatting, namespaces, and common anti-patterns. Defers to the host skill (`clojure-jvm`, `clojurescript`, or `clojuredart`) for runtime-specific guidance. |
| **clojure-lenses** | Auto-triggers alongside the [code-lenses](https://github.com/brackendev/code-lenses) plugin in Clojure work. Translates the four default code-lenses philosophies (grug, Honest Code, Tidy First, Parse Don't Validate) into idiomatic Clojure, plus the two opt-in philosophies (APOSD, Legacy Code) that activate when their lens is added with `+aposd` or `+legacy-code` or invoked directly. |

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

MIT. See [LICENSE](LICENSE).
