# clojure-skills

Clojure development skills packaged as an [APM](https://github.com/microsoft/apm) plugin. One install deploys the full set to every runtime in APM's default target set: Claude Code, Codex, OpenCode, Cursor, Copilot, Gemini, Windsurf, Kiro, and Grok Build. Antigravity is supported by naming it explicitly with `--target antigravity`.

Skills follow the [Agent Skills](https://agentskills.io) open standard. Five auto-trigger from conversation context (`clojure`, `clojure-lenses`, `clojure-repl`, `clojure-debug`, `clojure-deps`). The other two, `/clj-fix` and `/clj-smells-fix`, are slash commands, and the agent can also run them from a natural-language request. Runtimes differ in how they offer these commands: Codex lists them under `/skills` and runs them as `$name`, OpenCode attaches them with `@name`, and Gemini CLI also offers the auto-triggered skills as slash commands.

The `clojure` skill is the host-neutral baseline for the Clojure family. It triggers across `.clj`, `.cljs`, `.cljc`, and `.cljd`. Host-specific guidance (Java interop, refs / agents / STM, ClojureDart's `cljd.flutter` directives, Biff conventions) lives in companion packages so this skill stays applicable everywhere.

## Companion packages

Seven sibling APM packages. Install whichever match your project. Each carries the runtime-specific or framework-specific delta on top of this host-neutral baseline.

| Package | Focus | Layers on |
|---------|-------|-----------|
| [clojure-skills](https://github.com/brackendev/clojure-skills) (this package) | Host-neutral [Clojure](https://clojure.org/) family baseline: style, naming, threading, collections, atoms, dispatch, formatting, namespaces, and testing. Triggers on `.clj`, `.cljs`, `.cljc`, `.cljd`. | — |
| [clojure-jvm-skills](https://github.com/brackendev/clojure-jvm-skills) | [Clojure](https://clojure.org/) on the JVM: Java interop, refs / agents / STM, `with-open`, JVM-typed exceptions, `alter-var-root`, and the Clojure CLI / `tools.build` / `clj-kondo` / `cljfmt` / `test-runner` / nREPL workflow. | `clojure-skills` |
| [clojurescript-skills](https://github.com/brackendev/clojurescript-skills) | [ClojureScript](https://clojurescript.org/) style and tooling: JavaScript interop, externs inference, macro stage separation, `catch :default`, JS-flavored numbers and truthiness, and the `cljs.main` workflow. Triggers on `.cljs`, `.cljc` compiled to JS, `shadow-cljs.edn`, `figwheel-main.edn`. | `clojure-skills` |
| [biff-skills](https://github.com/brackendev/biff-skills) | [Biff](https://biffweb.com/) 2 web framework on the JVM: conventions and deployment. | `clojure-skills` + `clojure-jvm-skills` |
| [electric-skills](https://github.com/brackendev/electric-skills) | [Electric](https://github.com/hyperfiddle/electric) v3 full-stack reactive framework: `e/defn` components, `e/client` / `e/server` site transfer, `e/for` over `e/diff-by`, `e/watch` state, `e/Token` transactions, `e/Offload`, the `electric-dom3` and `electric-forms5` APIs, and the single-JVM shadow-cljs workflow. Triggers on `hyperfiddle.electric3`, `hyperfiddle.electric-dom3`, `e/defn`, `e/client`, `e/server`, and `com.hyperfiddle/electric`. | `clojure-skills` + `clojurescript-skills` + `clojure-jvm-skills` |
| [fulcro-skills](https://github.com/brackendev/fulcro-skills) | [Fulcro](https://github.com/fulcrologic/fulcro) 3 full-stack framework: `defsc` components, idents and the normalized client database, mutations, `df/load!`, dynamic routing, forms, UI state machines, Fulcro Inspect, and the Pathom 3 server. Triggers on `com.fulcrologic.fulcro.*`, `com.fulcrologic.rad.*`, `com.wsscode.pathom3.*`, `defsc`, `defmutation`, `defrouter`, `df/load!`, and ident vectors. | `clojure-skills` + `clojurescript-skills` + `clojure-jvm-skills` |
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
- [clj-kondo](https://github.com/clj-kondo/clj-kondo) for the lint steps in the user-invoked skills below and for the `edit-lint` hook.
- [Babashka](https://babashka.org) (`bb`) to run the `edit-lint` hook. Without it, the hook does nothing and prints a notice on standard error.
- The `clj-fix` dry step requires a [dry4clj](https://github.com/unclebob/dry4clj) `:dry4clj` alias in `deps.edn`. dry4clj is not published to Clojars, so the alias uses a Git coordinate. Without the alias, the dry step reports that it was skipped and shows the alias to add.

## Command guide

A quick guide to every slash command. The detailed entries under [Skills](#skills) cover arguments and examples.

| Command | Use it when | What it does |
|---------|-------------------|--------------|
| `/clj-fix` | Lint, format, or tests need attention | Runs the quality pipeline, applying safe lint fixes and rewriting formatting with `cljfmt fix` |
| `/clj-smells-fix` | Clojure code has smells to address | Applies the safe mechanical and DEFECT-band fixes and reports the rest |

## Skills

### Quality

#### `/clj-fix [lint|format|test|dry] [--report]`

Run the Clojure quality pipeline. Defaults to the full sequence (lint, format, test, dry). The `lint` step applies safe mechanical fixes and the `format` step rewrites files with `cljfmt fix`. The `test` and `dry` steps are pure-read. Pass `--report` to disable writes and preview the lint and format changes without applying them. Each step is also addressable on its own.

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
| **clojure-repl** | Evaluating code in a running REPL: nREPL, `.nrepl-port`, `.shadow-cljs/nrepl.port`, `clj-nrepl-eval`, clojure-mcp, `shadow/repl`, the ClojureDart socket REPL, `:reload`, or a request to try code in the REPL. Covers finding and verifying the project's REPL, choosing a client, evaluating small forms with bounded output, reloading, and confirming REPL results against the saved source. |
| **clojure-debug** | A failing program, REPL evaluation, build, or test: "Syntax error compiling", "Syntax error macroexpanding", "Syntax error reading source", "Execution error", "Could not locate ... on classpath", "did not conform to spec", `ExceptionInfo`, `ex-data`, or a stack trace. Covers keeping the evidence, finding the error phase and the root cause, reading `ex-data`, reproducing the failure, and verifying a focused fix. |
| **clojure-deps** | Researching a dependency: `deps.edn`, `project.clj`, or `shadow-cljs.edn` coordinates, Clojars, Maven Central, cljdoc, `find-versions`, or a question about which version a project uses or what the latest release is. Covers reading the resolved version, finding documentation and source for that exact version, and separating the latest release from a compatible one. |

## Hooks

The package includes one hook, `edit-lint`. After the agent edits a Clojure family file (`.clj`, `.cljs`, `.cljc`, `.cljd`, `.edn`, or `.bb`), the hook lints that file with clj-kondo and returns the findings to the agent in the same turn. An unbalanced delimiter or an unresolved symbol is therefore caught before the agent runs the code. The hook never changes a file.

- clj-kondo runs from the project root with the project's own `.clj-kondo` configuration and cache, so a linter the project turns off stays quiet. The project root is the nearest directory with `deps.edn`, `project.clj`, `shadow-cljs.edn`, `bb.edn`, or `.clj-kondo`, so a nested project uses its own configuration.
- The report lists errors first and shows at most twelve findings per file. A finding already reported for a file is not repeated in the same session. The report says that some findings may predate the latest edit, and it notes that an unresolved symbol can come from a macro that clj-kondo does not know.
- In `.cljd` files the hook reports syntax errors only. Without ClojureDart's exported configuration, clj-kondo reports Dart interop as unresolved.
- The hook skips files under `target/`, `node_modules/`, `.cpcache/`, `.shadow-cljs/`, `.clj-kondo/`, `cljd-out/`, `vendor/`, `third_party/`, `build/`, and `dist/`, and files that `.gitignore` excludes.
- The hook sees edits made through the runtime's file editing tools. A file changed by a shell command, such as `sed -i`, is not linted.
- When clj-kondo is not installed, the hook tells the agent once per session that it is inactive.

The hook has been run under these runtimes:

| Runtime | Behavior |
|---------|----------|
| Claude Code | Runs after `Write` and `Edit`. |
| Codex | Runs after `apply_patch`, for every file in the patch, once you trust the hook. Codex does not run a project hook until you approve it in an interactive session, and it records the approval in `~/.codex/config.toml`. |
| GitHub Copilot CLI | Runs from a separate copy, `copilot-hooks.json`, written in Copilot's format, after `create`, `edit`, and `apply_patch`. In prompt mode (`copilot -p`), Copilot loads the project-scope copy only in a trusted folder. In a trusted folder Copilot also runs the Claude Code copy, and the hook stays silent there, so the findings arrive once. |

The hook has not been run under the other runtimes. APM writes no native Cursor hook, so the install does not fail when Cursor also imports the Claude Code settings, but the hook has not been tested under Cursor. APM also installs the hook for Gemini CLI, Kiro, and Windsurf. The script recognizes only the edit tools named above, so it is expected to do nothing there, but this has not been tested. OpenCode and Grok Build have no hook support.

`apm uninstall` removes the hook with the package. A user-scope uninstall can leave behind an empty runtime configuration file that the install created, such as `~/.claude/settings.json` or `~/.codex/hooks.json`. Such a file names no hook command and is safe to delete or keep.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

MIT. See [LICENSE](LICENSE).
