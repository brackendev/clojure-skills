# Changelog

## [Unreleased]

### 0.1.13

#### Removed

- Extracted the ClojureDart skills (`clojuredart`, `clojuredart-lenses`, `cljd-nav`, `cljd-new`, `cljd-check`, `cljd-test`, `cljd-upgrade`, `cljd-smells-review`) into a separate package, [clojuredart-skills](https://github.com/brackendev/clojuredart-skills). Install that package alongside `clojure-skills` to keep ClojureDart coverage.

### 0.1.12

#### Changed

- Consolidated the previously separate `clojure` and `clojuredart` packages into a single APM-first plugin named `clojure-skills`. One `apm install brackendev/clojure-skills` now deploys all thirteen skills to every runtime APM supports (Claude Code, Codex, OpenCode, Cursor, Copilot, Gemini, Windsurf).
- Canonical skill source lives at `.apm/skills/<name>/`. Each skill ships `SKILL.md` plus `agents/openai.yaml` with the Clojure logo blue brand color (`#5881D8`); supporting `references/` files are preserved where they existed.
- Added `.opencode/skills/<name>/SKILL.md` as byte-identical mirrors of the canonical source for local OpenCode validation.

#### Removed

- Removed the legacy two-plugin layout (`claude/`, `codex/`, `.claude-plugin/marketplace.json`, `.agents/plugins/marketplace.json`, and the per-plugin `plugin.json` files). These flipped the APM lockfile classification from `apm_package` to `marketplace_plugin` and suppressed skill deployment to every runtime.

### clojure 0.1.6

#### Added

- **clj-check** (user-invoked): Added a `dry` step that scans for duplicate top-level forms with [dry4clj](https://github.com/unclebob/dry4clj). The step runs as part of the default pipeline (now `lint, format, test, dry`) and is also addressable on its own (`/clj-check dry`). dry4clj exits 0 whether or not it finds candidates, so the step inspects stdout and reports pass only when it contains the literal `No duplicate candidates found.`. The project's `deps.edn` must define a `:dry4clj` alias.

### clojuredart 0.1.11

#### Added

- **cljd-check** (user-invoked): Added a `dry` step that scans for duplicate top-level forms with [dry4clj](https://github.com/unclebob/dry4clj). The step runs as part of the default pipeline (now `lint, format, compile, dry`) and is also addressable on its own (`/cljd-check dry`). dry4clj exits 0 whether or not it finds candidates, so the step inspects stdout and reports pass only when it contains the literal `No duplicate candidates found.`. The skill notes that upstream dry4clj scans `.clj`, `.cljc`, and `.cljs` files only; covering `.cljd` requires a build with `.cljd` added to `dry4clj.core/source-extensions` (see brackendev/dry4clj branch `add-cljd-extension`, upstream PR #1).

### clojure 0.1.5

#### Added

- **clj-smells-review** (user-invoked): Review Clojure code against the [clj-smells catalog](https://github.com/nufuturo-ufcg/clj-smells-catalog), a community catalog of 35 Clojure-specific code smells. Pairs `clj-kondo` static analysis with LLM-assisted detection for smells that static analysis cannot catch. Reports findings with severity tiers (`DEFECT`, `SMELL`, `HINT`) and a smell density verdict. Supports the same argument shape as code-lenses reviews: no argument reviews changed files, a path scopes to that path, `all` reviews the full codebase, and `fix` applies non-conflicting findings after the report. Catalog pinned to upstream commit `d1ae189` (2026-01-19); upstream changes require a digest refresh. Ships a clj-kondo overlay config (`references/clj-kondo-overlay.edn`) that enables `:refer-all`, `:redundant-do`, `:redundant-let`, and `:discouraged-var` entries for `clojure.lang.RT` methods. Falls back to LLM-only review when `clj-kondo` is not on PATH.
- **clj-smells-reviewer** (Claude agent): Delegating agent that runs the `clj-smells-review` skill. Enables future integration with `code-lenses`'s `/review-all` command via an explicit `+clj-smells` opt-in (tracked in TODO).

### clojuredart 0.1.10

#### Added

- **cljd-smells-review** (user-invoked, placeholder): Scaffolds a future ClojureDart-specific smells review. Currently prints a "not yet implemented" notice and exits. Placeholder lists planned categories (dynamic warnings, Dart interop, Flutter directive misuse, widget rebuild behavior, async patterns, generated files, project config). Ships now to reserve the command name; implementation is tracked in TODO.

### clojuredart 0.1.9

#### Fixed

- **clojuredart** (model-invoked): Corrected the REPL beta-stability note. Prior text claimed a client disconnect killed the server's write thread, that subsequent TCP connections were accepted but never received responses, and that `clj -M:cljd flutter` had to be restarted to recover. Hands-on testing on the Android emulator showed the opposite: the form evaluates and the response reaches the client before the `Error: Write end dead` flood appears, and a fresh `nc` connection evaluates forms and receives responses without restarting. The limitation now describes the flood as cosmetic log noise rather than a blocker.
- **clojuredart** (model-invoked): Removed the "short pipes trigger the bug" bullet. With a brief `sleep` before stdin EOFs, a single-form pipeline like `(echo '(+ 1 2)'; sleep 1) | nc localhost <port>` works; the Driving the REPL from a Script section now reflects this and reframes the subshell pattern as the right choice for multi-form sessions that interleave external side effects, not as a bug workaround.

### clojuredart 0.1.8

#### Fixed

- **clojuredart** (model-invoked): Corrected `*env*` to `*env` (no trailing asterisk) in the Interactive Widget Inspection example. The upstream README defines the var as `*env`, bound after `pick!`.
- **clojuredart** (model-invoked): Removed the misleading `(require '[cljd.flutter.repl :as repl])` preamble and `repl/pick!` / `repl/mount!` calls. The skill already stated that `pick!` and `mount!` are auto-referred in `cljd.user`; the example now matches that and the upstream README.

#### Changed

- **clojuredart** (model-invoked): Added the upstream-documented `(keys *env)` idiom for exploring a picked widget's lexical bindings, `(ns my.app.core)` for switching namespaces in the REPL, and the Emacs `C-u M-x inferior-lisp` client alternative.

### clojuredart 0.1.7

#### Changed

- **clojuredart** (model-invoked): Expanded the REPL section. Added the announcement line printed by `clj -M:cljd flutter` (`=== 🤫 ClojureDart REPL === listening on port === N ===`), a "Driving Live App State" example showing `swap!` on a `:watch`-backed atom to re-render the UI without a file edit, and a "Driving the REPL from a Script" section with the working subshell pattern for non-interactive use. Expanded limitations: native Dart VM only (no REPL port on `-d chrome`), and the beta stability issue where a client disconnect during a write kills the server's output thread with `SocketException: Broken pipe` and requires restarting `clj -M:cljd flutter` to recover.
- **clojuredart** (model-invoked): Frontmatter description now lists `REPL usage` and `REPL-driven development` as triggers so REPL questions auto-match the skill.

### clojuredart 0.1.6

#### Added

- **cljd-new** (user-invoked): Scaffolds clj-kondo lint setup. Writes `.clj-kondo/config.edn` with a `cljd.test/deftest` hook, Dart interop exclusions (`String?`, `DateTime?`, `Uri`, `int`, `double`, `DateTime`), and the `:flutter/widget` custom linter downgraded to `:warning` so future directive drift does not block builds. Writes `.clj-kondo/hooks/cljd_test.clj`. Runs `clj-kondo --copy-configs --dependencies` to import the upstream `tensegritics/clojuredart` hooks. Calls out two upstream hook gaps to patch when encountered: `:default`/`:value>`/`:dispose-value` options on `:watch`, and 3-form `catch` blocks.

#### Changed

- **cljd-check** (user-invoked): Bootstraps upstream clj-kondo exports when missing, then lints both `src` and `test` (previously `src` only). Without the upstream hooks, `cljd.flutter/widget` forms generate hundreds of false positives and the check is unusable.
- **clojuredart** (model-invoked): Replaced the vague "`:watch nil` is a false positive" gotcha with a pointer to the real lint setup. Upstream already handles nil initial values; the remaining noise is entirely about missing the clj-kondo hook configuration.

### clojuredart 0.1.5

#### Changed

- **clojuredart** (model-invoked): Split Dynamic Warnings gotcha into two flavors: inference failures (fix with type hints) and resolution failures (`can't resolve member ...`, fix the wrong name or wrong type). Added a Common Mistakes entry warning against Python-style method names like `.__setitem` and `.__getitem` on Dart objects, with a pointer to the `(. m "[]=" k v)` operator syntax for Dart Map mutation.
- **cljd-check** (user-invoked): Compile step now fails when output contains `DYNAMIC WARNING: can't resolve member` lines, even with exit code 0. These warnings indicate a method or property that does not exist on the target type and will throw `NoSuchMethodError` at runtime.

### clojuredart 0.1.4

#### Removed

- Remove `effort` frontmatter from all skills (both Claude and Codex packages)

### clojure 0.1.4

#### Removed

- Remove `effort` frontmatter from all skills (both Claude and Codex packages)

### clojuredart 0.1.3

#### Added

- Added `effort` frontmatter to all applicable skills. Complex skills (clojuredart-lenses, cljd-upgrade, cljd-test) use `effort: max` for deep reasoning. Routine skills (cljd-check, cljd-new, cljd-nav) use `effort: auto` for adaptive thinking. The clojuredart model-invoked style guide has no effort set and runs inline.

### clojure 0.1.3

#### Added

- Added `effort` frontmatter to all applicable skills. Complex skills (clojure-lenses) use `effort: max` for deep reasoning. Routine skills (clj-check, clj-new) use `effort: auto` for adaptive thinking. The clojure model-invoked style guide has no effort set and runs inline.

### clojuredart 0.1.2

#### Changed

- **Packaging:** Added an APM-detectable root `plugin.json` to the Codex package so `apm install --target codex brackendev/clojure-skills/codex/clojuredart` works for per-project installs.
- **clojuredart** (model-invoked): Moved Key Rules to the top of the skill for front-loaded context. Moved project workflows (MCP integration, CLI, project structure, deps.edn, tooling) to `references/project-workflows.md` to reduce always-loaded context size. Added pointer to references file after Key Rules. Removed redundant "When to Apply" section (covered by description frontmatter). Added directive decision guide for choosing between atoms, cells, `:bind`/`:get`, `:managed`, `:watch`, and `:bg-watcher`. Fixed Clojure version in deps.edn reference (1.11.0 to 1.12.0).
- **cljd-nav** (model-invoked): Removed redundant "When to Apply" section (covered by description frontmatter).
- **cljd-test** (user-invoked): Added mode-to-command table mapping `unit`, `widget`, and `all` arguments to specific `dart test` tag commands. Removed one-off `allowed-tools` frontmatter for consistency with other skills.

### clojure 0.1.2

#### Changed

- **Packaging:** Added an APM-detectable root `plugin.json` to the Codex package so `apm install --target codex brackendev/clojure-skills/codex/clojure` works for per-project installs.
- **clojure** (model-invoked): Expanded style guide coverage from bbatsov/clojure-style-guide. Moved Key Rules to the top of the skill for front-loaded context. Moved project workflows (CLI, MCP integration, project structure, deps.edn, tooling, REPL) to `references/project-workflows.md` to reduce always-loaded context size. Added laziness and realization guidance (run!, doseq, mapv, doall). Added nil-safe threading (some->, some->>, if-some, when-some). Added dispatch choice guidance (case/cond versus multimethods versus protocols). Added namespaced keys and boundary parsing with spec/Malli. Added comment form REPL workflows. Added naming conventions for side-effecting functions (`!`), CapitalCase for protocols/records/types, constants, and idiomatic parameter names. Added function design guidance for higher-order functions over loop/recur, positional parameter limits, pre/post conditions, flexible comparisons, function literals, and anonymous functions over comp/partial. Added collection idioms for vec over into, list* over cons, destructuring over index access, and record constructors. Corrected commas guidance to allow optional commas in maps. Added state management for refs, agents (send versus send-off), and io! macro. Added macro best practices for thin sugar over functions. Added comment conventions and #_ reader macro. Added namespace guidance for sorting requires, idiomatic aliases, and single-segment avoidance. Added new sections for privacy and metadata, exception handling, testing conventions, and docstrings. Softened absolute language for function length, pre/post conditions, and formatting rules.

### clojuredart 0.1.1

#### Added

- **cljd-test** (user-invoked): Scaffold and run ClojureDart tests with cljd.test. Covers unit tests, widget tests with flutter_test runner, integration tests, tag-based filtering, and deps.edn test configuration.
- **cljd-nav** (model-invoked): Navigation patterns for ClojureDart Flutter applications. Covers named routes, go_router integration with nested navigation, Navigator API for dialogs and modals, and tab navigation with BottomNavigationBar and TabBar.
- **clojuredart-lenses** (model-invoked): Translate code-lenses design philosophies (grug, APOSD, Tidy First, Parse Don't Validate, Honest Code, Legacy Code) to idiomatic ClojureDart and Flutter patterns.

#### Changed

- **clojuredart** (model-invoked): Expanded async coverage with Streams, isolates, and async function syntax. Added cells dependency chains, scoped watches for rebuild optimization, and f/widget vs f/build decision guide. Added Dart package integration workflow, platform channels and FFI reference, REPL interactive inspection with pick!/mount!, and Gotchas section covering dynamic warnings, Flutter Web issues, and common mistakes.

## 0.1.0

Restructure repository for multi-platform support. Plugins are now grouped under `claude/` and `codex/` directories. All plugin versions reset to 0.1.0.

### clojure 0.1.0

- **clojure** (model-invoked): Curated Clojure style guide rules from bbatsov/clojure-style-guide, naming conventions, idiomatic patterns, threading macros, collection idioms, state management, and common anti-patterns.
- **clj-new** (user-invoked): Scaffold a new Clojure project with deps.edn, test runner, tools.build, and cljfmt.
- **clj-check** (user-invoked): Run the Clojure quality pipeline (clj-kondo lint, cljfmt format, test runner).
- **clojure-lenses** (model-invoked): Translate code-lenses design philosophies (grug, APOSD, Tidy First, Parse Don't Validate, Honest Code, Legacy Code) to idiomatic Clojure patterns.

### clojuredart 0.1.0

- **clojuredart** (model-invoked): Core ClojureDart knowledge including Dart interop syntax, type system, cljd.flutter directives, class creation, async, destructuring patterns, cells, REPL, and CLI reference.
- **cljd-new** (user-invoked): Scaffold a new ClojureDart Flutter project with deps.edn, entry point, formatting config, and initial compile.
- **cljd-check** (user-invoked): Run the ClojureDart quality pipeline (clj-kondo lint, cljfmt format, ClojureDart compile).
- **cljd-upgrade** (user-invoked): Upgrade ClojureDart dependency in deps.edn to the latest commit.
