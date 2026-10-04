# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.31] - 2026-10-04

### Changed

- The `clj-new` scaffold now pins Clojure 1.12.6, cljfmt 0.16.6, and tools.build v0.10.14.

### Fixed

- The `clj-new` scaffold's `.cljfmt.edn` now uses `:extra-indents` instead of `:indents`. The old key replaced cljfmt's default indentation rules, so `cljfmt check` failed on the scaffold's own test file and `cljfmt fix` mis-indented `deftest` and `testing` bodies.
- The uberjar built by the `clj-new` scaffold now runs with `java -jar`. The entry namespace now declares `(:gen-class)`, which the build needs to generate the main class.
- A freshly scaffolded `clj-new` project now lints without warnings. The template's `-main` names its unused rest argument `_args`.

## [0.1.30] - 2026-09-26

### Fixed

- The `clj-smells-fix` Stage 1 instructions now list exactly the clj-kondo findings the skill rewrites: `:refer :all`, `:use`, redundant `do`, nested `let`, unused `:require` entries, and unused `let` bindings. The skill no longer tries to rewrite excessive explicit refers, which have no mechanical fix, and it no longer rewrites unused function parameters or destructuring bindings. The catalog digest now names the `:use` linter that the overlay enables for Excessive Refers.
- The `clj-smells-fix` load-time side-effect fix now updates callers to dereference the new `delay` instead of renaming them.
- Under `--report`, the `clj-smells-fix` report now lists would-be fixes under a "Would apply" heading, as the skill's report rules describe.
- The `clj-fix` dry step reports `SKIPPED` rather than `SKIP` when no production source path exists, matching the summary block.
- The `clj-fix` vendored-path note no longer describes a path argument that the skill does not accept.
- The `clj-fix`, `clj-new`, and `clj-smells-fix` skills now link to the package's `CONVENTIONS.md` on GitHub, because the file is not installed alongside the skills.
- The `clojure` skill's `if-not` example now uses two branches, consistent with the rule to use `when` for single-branch conditionals. The pre and post conditions section now reserves `:pre` for internal invariants and uses `ex-info` for input validation, consistent with the skill's exception guidance.

### Changed

- The `clojure` skill lists `fulcro-skills` among its companion packages and points to its host-neutral REPL conventions reference.

## [0.1.29] - 2026-09-16

### Changed

- The README's runtime sentence now names Grok Build after Kiro. APM 0.28.0 added `grok-build` as a canonical target that `apm install --target all` includes and that APM auto-detects from a `.grok/` directory, so the default target set now has nine runtimes. Grok Build keeps a target-native skill directory (`.grok/skills/` at project scope, `~/.grok/skills/` at user scope), like Claude Code and Kiro, rather than the shared `.agents/skills/` directory.

## [0.1.28] - 2026-07-29

### Changed

- The README's runtime sentence now scopes its list to APM's default target set rather than claiming every runtime APM supports. APM 0.26.0 also supports Antigravity, IntelliJ, and several experimental runtimes, none of which `apm install --target all` includes. The eight runtimes named are unchanged, and the sentence adds that Antigravity works when named explicitly with `--target antigravity`.

## [0.1.27] - 2026-07-29

### Removed

- The package manifest no longer declares the top-level `target: all` field. The APM manifest schema deprecates the `all` value: a parser treats the field as though it were absent and falls through to the `--target` flag or filesystem auto-detection, and the value is scheduled to become a hard parse error in a future APM release. Removing the field makes that fall-through behavior permanent. Installation behavior is unchanged, because APM already resolved targets by auto-detection rather than from this field. The separate `compilation.target` setting is not affected.

## [0.1.26] - 2026-06-23

### Changed

- The `clj-fix` lint step now applies safe mechanical fixes by default instead of only reporting. It applies the Stage 1 mechanical band that `clj-smells-fix` owns (redundant `do`, nested `let` and `when-let`, `:refer :all`, `:use`, unused `:require` entries, unused let-bindings), reports direct `clojure.lang.RT` usage without rewriting it, and writes nothing under `--report`. The lint and format steps now both write by default, while the test and dry steps remain pure-read. The clj-fix worked example in CONVENTIONS.md was updated to match.

## [0.1.25] - 2026-06-23

### Changed

- The `clj-fix` dry step and the README now point the `:dry4clj` alias at `unclebob/dry4clj` rather than the `brackendev/dry4clj` fork. Upstream dry4clj carries the `.cljd` source-extension support and the `--edn` output the family relies on, so the fork is no longer required. The alias setup is otherwise unchanged.

## [0.1.24] - 2026-06-15

### Changed

- Add Kiro to the README's runtime list. APM 0.20.0 added Kiro as a first-class install target included in `apm install --target all`, so the README now lists it alongside Claude Code, Codex, OpenCode, Cursor, Copilot, Gemini, and Windsurf.

## [0.1.23] - 2026-06-02

### Changed

- The `clj-fix` dry step and the README now point the `:dry4clj` alias at the `brackendev/dry4clj` fork rather than `unclebob/dry4clj`. The fork is the maintained source for this family and carries the `.cljd` source-extension support the ClojureDart companion relies on. The alias setup is otherwise unchanged.
- The `clj-fix` dry step is now advisory and scoped to production source. It scans the source paths declared by the project's build configuration (for example `deps.edn` `:paths`) rather than a hardcoded `src test`, reads dry4clj's EDN output instead of matching a status string, and reports candidates without failing the run or halting the pipeline. The step reports `PASS` when no candidates are found, `REVIEW` when it lists candidates for inspection, `ERROR` when the scan cannot run or parse, or `SKIPPED`. Test directories are excluded by default because repeated test scaffolding is usually intentional, and lint, format, and test remain the pipeline's pass and fail gates.

## [0.1.22] - 2026-05-28

### Added

- `CONVENTIONS.md` gains Rule 4: vendored and generated paths are excluded by default from mutating skills that walk the workspace. The rule sits alongside the existing three rules (renamed from "The three rules" to "The four rules"). Two filters apply together (`.gitignore` matches plus a hardcoded floor of dependency directories, build outputs, and lock files). The override rides on Rule 1's existing `<path>` `<glob>` grammar; no new flag is introduced. Applies to `/clj-fix` and `/clj-smells-fix`. Exempt: `/clj-new` is scaffolding.
- `/clj-fix` and `/clj-smells-fix` each extend their `## Mutation` section with a paragraph restating Rule 4 in context. Operators see the policy without leaving the skill's page.

## [0.1.21] - 2026-05-26

### Fixed

- Quote the YAML `description` and `argument-hint` frontmatter in all skills that had unquoted values. Prevents potential YAML misinterpretation of special characters (angle brackets in `argument-hint`, semicolons and parentheses in `description`).

## [0.1.20] - 2026-05-22

### Changed

- The `clj-smells-review` skill is renamed to `clj-smells-fix` and adopts a mutating contract that matches the `*-fix` family. Stage 1 mechanical findings from the `clj-kondo` overlay (`:refer :all` expansion, `:use` rewrite, redundant `do`, nested `let` / `when-let`, unused `:require` entries, unused bindings) are applied via Edit by default. A narrow safety band of Stage 2 `DEFECT`-tier findings is also auto-applied: macro double-evaluation (rebind in a `let`), unwrapped resource handles (wrap in `with-open` when the type is in scope), blocking forms inside `go` blocks (`<!!`/`>!!` rewritten to `<!`/`>!`), and load-time side effects in `def` bodies (wrap in `delay`, rewrite callers when all are in scope). Direct `clojure.lang.RT` usage, `SMELL`-tier findings, `HINT`-tier findings, and `DEFECT`-tier findings outside the safety band remain report-only. The skill accepts `--report` to disable all writes and produce the previous pure-report output. Operators with a saved `/clj-smells-review` invocation should replace it with `/clj-smells-fix`; add `--report` to retain the prior behavior.

## [0.1.19] - 2026-05-20

### Changed

- `CONTRIBUTING.md` Layout table now includes a row for `CONVENTIONS.md`, aligning the package with the family-wide structural template.

## [0.1.18] - 2026-05-20

### Changed

- The `clj-tidy` skill is renamed to `clj-fix`. The verb adopts the family-wide noun-first canonical naming (`<target>-<verb>`), where the trailing verb signals behavior. The `fix` suffix matches the cross-language `fix-*` skills in `project-skills` and `code-lenses`, replacing the `tidy` verb that was specific to the Clojure family. Operators with a saved `/clj-tidy` invocation should replace it with `/clj-fix`. Step keywords (`lint`, `format`, `test`, `dry`), the `--report` flag, and the rest of the behavior are unchanged.
- The package `CONVENTIONS.md` now lists command suffixes (`-fix`, `-sync`, `-review`, and the rest) rather than verb-first patterns, reflecting the family's noun-first canonical naming.

## [0.1.17] - 2026-05-19

### Added

- A repo-root `CONVENTIONS.md` that defines the argument grammar, scope vocabulary, and mutation defaults every user-invocable skill in this package follows. Three rules cover argument grammar (one sanctioned flag, `--report`), scope vocabulary (`(no argument)`, `all`, `<path>`), and mutation-as-default. The document lists `/clj-new` as the standard's positional-required exemption and includes an author checklist that runs against every migrated skill.

### Changed

- The `clj-check` skill is renamed to `clj-tidy`. The verb now matches the default behavior: `cljfmt fix` runs by default and rewrites files in the `format` step. Operators with a saved `/clj-check` invocation should replace it with `/clj-tidy`. The new `--report` flag swaps the format step for `cljfmt check`, which previews diffs without writing; `lint`, `test`, and `dry` are pure-read regardless.
- The `clj-smells-review` skill becomes a pure-report skill. The `## Inputs and Scope` section is renamed `## Arguments` and uses the canonical scope vocabulary (`(no argument)`, `all`, `<path>`). The frontmatter `description` now states "pure report, never writes." Operators with a saved `/clj-smells-review ... fix` invocation should drop the `fix` keyword and apply suggestions manually.
- The `clj-new` skill adds a `## Arguments` section that documents its positional `<project-name>` exemption and a `## Mutation` section that lists the files it writes.

### Removed

- The `fix` modifier on `clj-smells-review`. The skill no longer applies catalog findings to source files. Apply suggestions manually or run them through a separate tool. This is a breaking change for operators who relied on `fix`.

## [0.1.16] - 2026-05-17

### Changed

- The `clojure` baseline `SKILL.md` now lists [clojurescript-skills](https://github.com/brackendev/clojurescript-skills) alongside `clojure-jvm-skills` and `clojuredart-skills` as a published companion. The trigger in `README.md` for the `clojure` skill now reads "Defers to the host skill (`clojure-jvm`, `clojurescript`, or `clojuredart`)" instead of naming `clojurescript` as a future package.
- The `clojure-lenses` skill description and `README.md` row now reflect the upstream code-lenses default-versus-opt-in split: `grug`, `Honest Code`, `Tidy First`, and `Parse Don't Validate` are the default lenses; `APOSD` and `Legacy Code` are opt-in (`+aposd`, `+legacy-code`, or direct invocation). The body still contains APOSD and Legacy Code translations so the lens can apply them when explicitly invoked.

## [0.1.15] - 2026-05-17

### Added

- A `REPL-Driven Development` section in the `clojure` baseline `SKILL.md`. The section states the principle (REPL-first workflow, file edits follow working REPL code) and the recovery step (start a REPL when none is reachable), and points at the host skill for the dialect-specific command. This loads up front, so agents see the principle without having to load the references directory.

## [0.1.14] - 2026-05-17

### Changed

- The `clojure` skill is now the host-neutral baseline for the Clojure family. It triggers across `.clj`, `.cljs`, `.cljc`, and `.cljd` files and `clojure.core` forms in any dialect. JVM-specific guidance (Java interop, `with-open`, refs / agents / STM, `io!`, `alter-var-root`, JVM-typed exceptions, JVM-only aliases, the Clojure CLI / `tools.build` / `clj-kondo` / `cljfmt` / `test-runner` / nREPL workflow) has been extracted into the separate [clojure-jvm-skills](https://github.com/brackendev/clojure-jvm-skills) package. Install that package alongside `clojure-skills` for JVM Clojure work.
- The `clojure` skill now opens with an explicit applicability block naming the host skills (`clojure-jvm`, `clojuredart`, future `clojurescript`) and the override boundary, so host skills only override in named domains (interop, exceptions, resources, runtime-specific concurrency primitives, host aliases, host-specific tooling).
- Trimmed `references/project-workflows.md` to host-neutral REPL conventions (`(comment ...)` blocks, `in-ns`, fully-qualified cross-namespace references). The JVM CLI / `deps.edn` / `tools.build` / lint / format / test-runner / nREPL guidance moved to `clojure-jvm-skills/.apm/skills/clojure-jvm/references/project-workflows.md`.

### Removed

- Removed the Java Interop section, the `with-open` guidance, the "Never catch `Throwable`" rule, the refs / agents / STM / `io!` subsections in State Management, the `alter-var-root` rule, the JVM-typed exception examples, and the `(:import ...)` example. All moved to [clojure-jvm-skills](https://github.com/brackendev/clojure-jvm-skills).
- Removed the "Does not apply to ClojureDart projects" exclusion. The baseline now applies to ClojureDart and to every other Clojure dialect.

## [0.1.13] - 2026-05-17

### Removed

- Extracted the ClojureDart skills (`clojuredart`, `clojuredart-lenses`, `cljd-nav`, `cljd-new`, `cljd-check`, `cljd-test`, `cljd-upgrade`, `cljd-smells-review`) into a separate package, [clojuredart-skills](https://github.com/brackendev/clojuredart-skills). Install that package alongside `clojure-skills` to keep ClojureDart coverage.

## [0.1.12] - 2026-05-16

### Changed

- Consolidated the previously separate `clojure` and `clojuredart` packages into a single APM-first plugin named `clojure-skills`. One `apm install brackendev/clojure-skills` now deploys all thirteen skills to every runtime APM supports (Claude Code, Codex, OpenCode, Cursor, Copilot, Gemini, Windsurf).
- Canonical skill source lives at `.apm/skills/<name>/`. Each skill ships `SKILL.md` plus `agents/openai.yaml` with the Clojure logo blue brand color (`#5881D8`); supporting `references/` files are preserved where they existed.
- Added `.opencode/skills/<name>/SKILL.md` as byte-identical mirrors of the canonical source for local OpenCode validation.

### Removed

- Removed the legacy two-plugin layout (`claude/`, `codex/`, `.claude-plugin/marketplace.json`, `.agents/plugins/marketplace.json`, and the per-plugin `plugin.json` files). These flipped the APM lockfile classification from `apm_package` to `marketplace_plugin` and suppressed skill deployment to every runtime.

## Pre-split history

The entries below predate the split of this repository into separate
packages. At the time the `clojure` and `clojuredart` plugins were versioned
independently in this one file, so their version numbers overlap and are kept
under their original names.

### clojure 0.1.6 - 2026-05-07

#### Added

- **clj-check** (user-invoked): Added a `dry` step that scans for duplicate top-level forms with [dry4clj](https://github.com/unclebob/dry4clj). The step runs as part of the default pipeline (now `lint, format, test, dry`) and is also addressable on its own (`/clj-check dry`). dry4clj exits 0 whether or not it finds candidates, so the step inspects stdout and reports pass only when it contains the literal `No duplicate candidates found.`. The project's `deps.edn` must define a `:dry4clj` alias.

### clojuredart 0.1.11 - 2026-05-07

#### Added

- **cljd-check** (user-invoked): Added a `dry` step that scans for duplicate top-level forms with [dry4clj](https://github.com/unclebob/dry4clj). The step runs as part of the default pipeline (now `lint, format, compile, dry`) and is also addressable on its own (`/cljd-check dry`). dry4clj exits 0 whether or not it finds candidates, so the step inspects stdout and reports pass only when it contains the literal `No duplicate candidates found.`. The skill notes that upstream dry4clj scans `.clj`, `.cljc`, and `.cljs` files only; covering `.cljd` requires a build with `.cljd` added to `dry4clj.core/source-extensions` (see brackendev/dry4clj branch `add-cljd-extension`, upstream PR #1).

### clojure 0.1.5 - 2026-04-25

#### Added

- **clj-smells-review** (user-invoked): Review Clojure code against the [clj-smells catalog](https://github.com/nufuturo-ufcg/clj-smells-catalog), a community catalog of 35 Clojure-specific code smells. Pairs `clj-kondo` static analysis with LLM-assisted detection for smells that static analysis cannot catch. Reports findings with severity tiers (`DEFECT`, `SMELL`, `HINT`) and a smell density verdict. Supports the same argument shape as code-lenses reviews: no argument reviews changed files, a path scopes to that path, `all` reviews the full codebase, and `fix` applies non-conflicting findings after the report. Catalog pinned to upstream commit `d1ae189` (2026-01-19); upstream changes require a digest refresh. Ships a clj-kondo overlay config (`references/clj-kondo-overlay.edn`) that enables `:refer-all`, `:redundant-do`, `:redundant-let`, and `:discouraged-var` entries for `clojure.lang.RT` methods. Falls back to LLM-only review when `clj-kondo` is not on PATH.
- **clj-smells-reviewer** (Claude agent): Delegating agent that runs the `clj-smells-review` skill. Enables future integration with `code-lenses`'s `/review-all` command via an explicit `+clj-smells` opt-in (tracked in TODO).

### clojuredart 0.1.10 - 2026-04-25

#### Added

- **cljd-smells-review** (user-invoked, placeholder): Scaffolds a future ClojureDart-specific smells review. Currently prints a "not yet implemented" notice and exits. Placeholder lists planned categories (dynamic warnings, Dart interop, Flutter directive misuse, widget rebuild behavior, async patterns, generated files, project config). Ships now to reserve the command name; implementation is tracked in TODO.

### clojuredart 0.1.9 - 2026-04-24

#### Fixed

- **clojuredart** (model-invoked): Corrected the REPL beta-stability note. Prior text claimed a client disconnect killed the server's write thread, that subsequent TCP connections were accepted but never received responses, and that `clj -M:cljd flutter` had to be restarted to recover. Hands-on testing on the Android emulator showed the opposite: the form evaluates and the response reaches the client before the `Error: Write end dead` flood appears, and a fresh `nc` connection evaluates forms and receives responses without restarting. The limitation now describes the flood as cosmetic log noise rather than a blocker.
- **clojuredart** (model-invoked): Removed the "short pipes trigger the bug" bullet. With a brief `sleep` before stdin EOFs, a single-form pipeline like `(echo '(+ 1 2)'; sleep 1) | nc localhost <port>` works; the Driving the REPL from a Script section now reflects this and reframes the subshell pattern as the right choice for multi-form sessions that interleave external side effects, not as a bug workaround.

### clojuredart 0.1.8 - 2026-04-24

#### Fixed

- **clojuredart** (model-invoked): Corrected `*env*` to `*env` (no trailing asterisk) in the Interactive Widget Inspection example. The upstream README defines the var as `*env`, bound after `pick!`.
- **clojuredart** (model-invoked): Removed the misleading `(require '[cljd.flutter.repl :as repl])` preamble and `repl/pick!` / `repl/mount!` calls. The skill already stated that `pick!` and `mount!` are auto-referred in `cljd.user`; the example now matches that and the upstream README.

#### Changed

- **clojuredart** (model-invoked): Added the upstream-documented `(keys *env)` idiom for exploring a picked widget's lexical bindings, `(ns my.app.core)` for switching namespaces in the REPL, and the Emacs `C-u M-x inferior-lisp` client alternative.

### clojuredart 0.1.7 - 2026-04-24

#### Changed

- **clojuredart** (model-invoked): Expanded the REPL section. Added the announcement line printed by `clj -M:cljd flutter` (`=== 🤫 ClojureDart REPL === listening on port === N ===`), a "Driving Live App State" example showing `swap!` on a `:watch`-backed atom to re-render the UI without a file edit, and a "Driving the REPL from a Script" section with the working subshell pattern for non-interactive use. Expanded limitations: native Dart VM only (no REPL port on `-d chrome`), and the beta stability issue where a client disconnect during a write kills the server's output thread with `SocketException: Broken pipe` and requires restarting `clj -M:cljd flutter` to recover.
- **clojuredart** (model-invoked): Frontmatter description now lists `REPL usage` and `REPL-driven development` as triggers so REPL questions auto-match the skill.

### clojuredart 0.1.6 - 2026-04-23

#### Added

- **cljd-new** (user-invoked): Scaffolds clj-kondo lint setup. Writes `.clj-kondo/config.edn` with a `cljd.test/deftest` hook, Dart interop exclusions (`String?`, `DateTime?`, `Uri`, `int`, `double`, `DateTime`), and the `:flutter/widget` custom linter downgraded to `:warning` so future directive drift does not block builds. Writes `.clj-kondo/hooks/cljd_test.clj`. Runs `clj-kondo --copy-configs --dependencies` to import the upstream `tensegritics/clojuredart` hooks. Calls out two upstream hook gaps to patch when encountered: `:default`/`:value>`/`:dispose-value` options on `:watch`, and 3-form `catch` blocks.

#### Changed

- **cljd-check** (user-invoked): Bootstraps upstream clj-kondo exports when missing, then lints both `src` and `test` (previously `src` only). Without the upstream hooks, `cljd.flutter/widget` forms generate hundreds of false positives and the check is unusable.
- **clojuredart** (model-invoked): Replaced the vague "`:watch nil` is a false positive" gotcha with a pointer to the real lint setup. Upstream already handles nil initial values; the remaining noise is entirely about missing the clj-kondo hook configuration.

### clojuredart 0.1.5 - 2026-04-23

#### Changed

- **clojuredart** (model-invoked): Split Dynamic Warnings gotcha into two flavors: inference failures (fix with type hints) and resolution failures (`can't resolve member ...`, fix the wrong name or wrong type). Added a Common Mistakes entry warning against Python-style method names like `.__setitem` and `.__getitem` on Dart objects, with a pointer to the `(. m "[]=" k v)` operator syntax for Dart Map mutation.
- **cljd-check** (user-invoked): Compile step now fails when output contains `DYNAMIC WARNING: can't resolve member` lines, even with exit code 0. These warnings indicate a method or property that does not exist on the target type and will throw `NoSuchMethodError` at runtime.

### clojuredart 0.1.4 - 2026-04-13

#### Removed

- Remove `effort` frontmatter from all skills (both Claude and Codex packages)

### clojure 0.1.4 - 2026-04-13

#### Removed

- Remove `effort` frontmatter from all skills (both Claude and Codex packages)

### clojuredart 0.1.3 - 2026-04-12

#### Added

- Added `effort` frontmatter to all applicable skills. Complex skills (clojuredart-lenses, cljd-upgrade, cljd-test) use `effort: max` for deep reasoning. Routine skills (cljd-check, cljd-new, cljd-nav) use `effort: auto` for adaptive thinking. The clojuredart model-invoked style guide has no effort set and runs inline.

### clojure 0.1.3 - 2026-04-12

#### Added

- Added `effort` frontmatter to all applicable skills. Complex skills (clojure-lenses) use `effort: max` for deep reasoning. Routine skills (clj-check, clj-new) use `effort: auto` for adaptive thinking. The clojure model-invoked style guide has no effort set and runs inline.

### clojuredart 0.1.2 - 2026-04-07

#### Changed

- **Packaging:** Added an APM-detectable root `plugin.json` to the Codex package so `apm install --target codex brackendev/clojure-skills/codex/clojuredart` works for per-project installs.
- **clojuredart** (model-invoked): Moved Key Rules to the top of the skill for front-loaded context. Moved project workflows (MCP integration, CLI, project structure, deps.edn, tooling) to `references/project-workflows.md` to reduce always-loaded context size. Added pointer to references file after Key Rules. Removed redundant "When to Apply" section (covered by description frontmatter). Added directive decision guide for choosing between atoms, cells, `:bind`/`:get`, `:managed`, `:watch`, and `:bg-watcher`. Fixed Clojure version in deps.edn reference (1.11.0 to 1.12.0).
- **cljd-nav** (model-invoked): Removed redundant "When to Apply" section (covered by description frontmatter).
- **cljd-test** (user-invoked): Added mode-to-command table mapping `unit`, `widget`, and `all` arguments to specific `dart test` tag commands. Removed one-off `allowed-tools` frontmatter for consistency with other skills.

### clojure 0.1.2 - 2026-04-07

#### Changed

- **Packaging:** Added an APM-detectable root `plugin.json` to the Codex package so `apm install --target codex brackendev/clojure-skills/codex/clojure` works for per-project installs.
- **clojure** (model-invoked): Expanded style guide coverage from bbatsov/clojure-style-guide. Moved Key Rules to the top of the skill for front-loaded context. Moved project workflows (CLI, MCP integration, project structure, deps.edn, tooling, REPL) to `references/project-workflows.md` to reduce always-loaded context size. Added laziness and realization guidance (run!, doseq, mapv, doall). Added nil-safe threading (some->, some->>, if-some, when-some). Added dispatch choice guidance (case/cond versus multimethods versus protocols). Added namespaced keys and boundary parsing with spec/Malli. Added comment form REPL workflows. Added naming conventions for side-effecting functions (`!`), CapitalCase for protocols/records/types, constants, and idiomatic parameter names. Added function design guidance for higher-order functions over loop/recur, positional parameter limits, pre/post conditions, flexible comparisons, function literals, and anonymous functions over comp/partial. Added collection idioms for vec over into, list* over cons, destructuring over index access, and record constructors. Corrected commas guidance to allow optional commas in maps. Added state management for refs, agents (send versus send-off), and io! macro. Added macro best practices for thin sugar over functions. Added comment conventions and #_ reader macro. Added namespace guidance for sorting requires, idiomatic aliases, and single-segment avoidance. Added new sections for privacy and metadata, exception handling, testing conventions, and docstrings. Softened absolute language for function length, pre/post conditions, and formatting rules.

### clojuredart 0.1.1 - 2026-04-05

#### Added

- **cljd-test** (user-invoked): Scaffold and run ClojureDart tests with cljd.test. Covers unit tests, widget tests with flutter_test runner, integration tests, tag-based filtering, and deps.edn test configuration.
- **cljd-nav** (model-invoked): Navigation patterns for ClojureDart Flutter applications. Covers named routes, go_router integration with nested navigation, Navigator API for dialogs and modals, and tab navigation with BottomNavigationBar and TabBar.
- **clojuredart-lenses** (model-invoked): Translate code-lenses design philosophies (grug, APOSD, Tidy First, Parse Don't Validate, Honest Code, Legacy Code) to idiomatic ClojureDart and Flutter patterns.

#### Changed

- **clojuredart** (model-invoked): Expanded async coverage with Streams, isolates, and async function syntax. Added cells dependency chains, scoped watches for rebuild optimization, and f/widget vs f/build decision guide. Added Dart package integration workflow, platform channels and FFI reference, REPL interactive inspection with pick!/mount!, and Gotchas section covering dynamic warnings, Flutter Web issues, and common mistakes.

### 0.1.0 - 2026-03-31

Restructure repository for multi-platform support. Plugins are now grouped under `claude/` and `codex/` directories. All plugin versions reset to 0.1.0.

#### clojure 0.1.0 - 2026-03-31

- **clojure** (model-invoked): Curated Clojure style guide rules from bbatsov/clojure-style-guide, naming conventions, idiomatic patterns, threading macros, collection idioms, state management, and common anti-patterns.
- **clj-new** (user-invoked): Scaffold a new Clojure project with deps.edn, test runner, tools.build, and cljfmt.
- **clj-check** (user-invoked): Run the Clojure quality pipeline (clj-kondo lint, cljfmt format, test runner).
- **clojure-lenses** (model-invoked): Translate code-lenses design philosophies (grug, APOSD, Tidy First, Parse Don't Validate, Honest Code, Legacy Code) to idiomatic Clojure patterns.

#### clojuredart 0.1.0 - 2026-03-31

- **clojuredart** (model-invoked): Core ClojureDart knowledge including Dart interop syntax, type system, cljd.flutter directives, class creation, async, destructuring patterns, cells, REPL, and CLI reference.
- **cljd-new** (user-invoked): Scaffold a new ClojureDart Flutter project with deps.edn, entry point, formatting config, and initial compile.
- **cljd-check** (user-invoked): Run the ClojureDart quality pipeline (clj-kondo lint, cljfmt format, ClojureDart compile).
- **cljd-upgrade** (user-invoked): Upgrade ClojureDart dependency in deps.edn to the latest commit.
