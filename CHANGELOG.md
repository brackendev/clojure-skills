# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.42] - 2026-10-29

### Added

- `clojure` skill: Key Rule 19, "Let catalog data own policy," and the `references/data-driven-design.md` reference with examples of declared relationships, one resolver function per question, no hardcoded fallbacks, and load-time validation. `clojure-lenses` points to the reference from the Honest Code construct note.
- `clj-smells-fix`: a local `SMELL` entry, Catalog Policy Duplicated in Code, marked as separate from the pinned upstream catalog. It is reported only with evidence of both the catalog and the branch, and never applied automatically.
- `clj-rules` hook: delivers the condensed CLJ-1 through CLJ-12 Iron Laws to the agent at session start, after `/clear` or a compaction, and on the first matching edit when the session started outside a matching project, with an instruction to load the `clojure` skill and the host skill for the project. Verified only under Claude Code. Copilot and Cursor receive no copy.

## [0.1.41] - 2026-10-29

### Changed

- The Install section requires APM 0.34.0 and installs it from PyPI with `uv tool install apm-cli==0.34.0` after removing any Homebrew copy. APM 0.35.0 is not supported because it can delete installed skill files ([microsoft/apm#3372](https://github.com/microsoft/apm/issues/3372)).

## [0.1.40] - 2026-10-25

### Added

- The `clojure` skill has a reader conditionals reference. It covers the platform tags, why a conditional with no matching branch reads nothing instead of `nil`, splicing with `#?@`, the file types that read conditionals, and the portable patterns for a conditional require and a catch-all `catch` type: `Exception` on the JVM, `:default` in ClojureScript, and `Object` in ClojureDart. It explains why `:clj` comes last among the platform tags in a file that ClojureDart also compiles.
- `/clj-fix` reports a `.cljfmt.edn` that writes a regular expression as `#"..."`, which cljfmt cannot read, and shows the `#re` form, without editing the file unless you confirm.
- `/clj-smells-fix` and `/clj-fix` report a symbol used as a `case` test constant, which `case` compares as a literal instead of evaluating it. The finding is listed for review, is never rewritten, and does not fail the lint step, because such a symbol can be deliberate.
- `/clj-smells-fix` detects calls to every `clojure.lang.RT` method that a public `clojure.core` function wraps, and to `RT/var`, `RT/iter`, `RT/classForName`, and `RT/loadClassForName`. Each finding names the public replacement, or says that none exists.
- The `clojure-repl` skill explains that clojure-mcp's recommended `:repl` profile provides only evaluation, port listing, and delimiter repair, and that clojure-mcp finds a REPL started after it without a restart. It also notes that nREPL leaves a stale `.nrepl-port` file when its process is killed, and that reloading a test namespace does not reload the source namespace it tests.

### Changed

- The `clojure` skill presents `update-vals`, `update-keys`, the `parse-*` functions, `random-uuid`, `abs`, and the `:as-alias` require option as core features on every host, without naming the Clojure release that introduced them.
- `/clj-smells-fix` follows the clj-smells catalog at commit `51abf4b` (2026-06-11), which documents 34 smells. The digest takes the catalog's current descriptions. Non-Idiomatic Record Construction flags only the `(Foo. ...)` interop constructor, Namespace Load Side Effects covers top-level `require` and `requiring-resolve` calls, Relying on Load-Time Side Effects covers top-level values computed from mutable state, and Refs in Dependency Vector covers the dependency vectors of React effect hooks, such as `reagent.hooks/use-effect`.
- The `/clj-fix` format step runs cljfmt through the first runner the project supports: a `:cljfmt` alias in `deps.edn`, the Leiningen plugin, the `cljfmt` binary, or a Clojure CLI tool. The test step runs the project's own test command: `clojure -X:test`, `clojure -M:test`, `bin/kaocha`, or `lein test`. Each step reports `SKIPPED` when the project has no way to run it.
- When a shadow-cljs project reads its dependencies from `deps.edn`, `/clj-fix` also lints and scans the `:extra-paths` of the aliases that `shadow-cljs.edn` names.
- The `clojure-repl` skill checks that a JVM REPL runs in the current checkout, passes code to `clj-nrepl-eval` through a heredoc, sets a session-wide print limit with `set!`, keeps nREPL on its default local address, and finds the ClojureDart REPL port in `REPL.lock`, noting that `clojure -M:cljd watch` and web targets write that file without serving a REPL.
- The `clojure-debug` skill recognizes the printing phase, reported as "Error printing return value", in which a lazy result throws while the REPL prints it.
- The `clojure-deps` skill explains that `find-versions` lists prereleases, that the Clojars `latest_version` field can name a snapshot, and which commit SHA to pin for an annotated Git tag.
- The README lists cljfmt among the requirements. It also states that the `edit-lint` hook leaves source files unchanged while clj-kondo updates its own cache.
- The `clojure` skill states its Iron Laws and references for every host, with examples that run unchanged on the JVM, in ClojureScript, and in ClojureDart. JVM examples (`with-open` over a reader, `BigDecimal` arithmetic, next.jdbc calls, `clojure.java.shell`, `babashka.process`, `System/nanoTime`, and `System/getenv`) are replaced with portable ones, such as lazy results of `binding` and `try`, HoneySQL queries, and argument vectors for external commands. Each topic points to the reference file in the JVM, ClojureScript, or ClojureDart skill that covers the host details.
- When `/clj-fix` finds no cljfmt runner, it shows the `:cljfmt` alias to add. The alias uses `:replace-deps`, so cljfmt runs without the project's dependencies and still works when one of them conflicts with cljfmt or cannot be resolved. The format step passes source paths in the form each runner accepts, explains that cljfmt ignores `.cljfmt.clj` and `cljfmt.clj` unless `--read-clj-config-files` is passed and asks before passing it, and the lint step formats the files it rewrites with the same runner.
- The skills run Clojure CLI commands as `clojure` instead of `clj`, because `clj` exits with "Please install rlwrap" where `rlwrap` is not installed.
- The README `/clj-fix` heading shows the `all` keyword, which the command already accepted.
- The `clojure-lenses` skill limits spec, Malli, Component, Integrant, and Mount to the JVM and ClojureScript, and Ring to the JVM, and suggests the ClojureDart equivalent. It says "host interop" instead of "Java interop", and that no Clojure host has Erlang-style supervisors.
- The `clojure-debug` skill says which host prints each error format it recognizes.
- `/clj-smells-fix` describes its `with-open` rewrite for each host: on the JVM `with-open` calls `.close`, in ClojureDart it calls `.dispose` unless a `:dispose` directive names another method, and in ClojureScript, which has no `with-open`, the finding is reported without a rewrite.

### Fixed

- The README states the Java requirement that Clojure documents, Java 8 or higher. It said Java 17 or higher.
- The README says that the auto-triggered skills have no slash commands, except in Gemini CLI. It said that they could not be invoked directly.
- `/clj-fix` no longer fails in the format step of a project whose `deps.edn` has no `:cljfmt` alias.
- `/clj-smells-fix` rewrites `<!!`, `>!!`, and `alts!!` in a `go` block only when the call is lexically inside the `go` body, because a parking operation inside a nested function throws. Its `with-open` rewrite realizes a lazy result inside the form, so the fixed code does not fail with "Stream closed".
- The clj-smells digest corrects several entries. Blocking Inside Go describes core.async 1.9.865, whose go blocks run on a cached thread pool, and recommends `io-thread` and the `clojure.core.async.go-checking` property. Nested Forms does not suggest chaining `when-let` or `if-let`, which accept one binding. Improper Emptiness Check replaces `(= 0 (count coll))` with `empty?`, not with `seq`, which inverted the test. Verbose Checks warns that `zero?`, `pos?`, and `neg?` throw on non-numbers. Private Multimethods shows `(defmulti ^:private ...)`, and Production `doall` treats realization inside `with-open`, `binding`, or `try` as correct.
- The `clojure` skill's host statements hold on every host. In-place `assoc!` keeps 8 of 20 entries on JVM Clojure and ClojureScript. ClojureDart has no `clojure.pprint`, rejects `:refer :all`, compiles `#'some.ns/var` to a symbol, and switches REPL namespaces with an `ns` form instead of `in-ns`. ClojureDart's `parse-long` accepts surrounding whitespace and a `0x` prefix, and ClojureScript reads `0.1M` as an ordinary JavaScript number. The skill also names how each host turns off pre and post conditions, and shows that `[clojure.edn :as edn]` works on every host.
- The `clojure` skill's `ns` example sorts its requires alphabetically, and the skill does not ask for requires grouped by origin, which conflicted with sorting them as the Clojure Style Guide shows.
- The `clojure` skill keeps the closing parenthesis of a `comment` form on its last form, which is how cljfmt formats it. The skill asked for the parenthesis on its own line, which the `/clj-fix` format step undid.
- The `clojure-repl` skill states that clojure-mcp also repairs unbalanced delimiters without reporting it, that `clj-nrepl-eval --reset-session` keeps defined vars, that `clj-nrepl-eval` exits 0 when an evaluation fails, and that `*ns*` is `nil` in a shadow-cljs ClojureScript session. It warns that Biff's `prod-nrepl` task writes the production port to `.nrepl-port` while it runs.
- The `clojure-lenses` skill names refs, agents, `send`, and `dosync` as JVM features, and `with-redefs` as unavailable in ClojureDart.
- The `edit-lint` hook no longer reports a reader conditional in a `.cljd` file as a syntax error. ClojureDart reads conditionals in `.cljd` files.
- The dry4clj alias that `/clj-fix` shows is indented the way cljfmt formats it, so the alias passes `cljfmt check` when pasted into `deps.edn`.
- The `clojure` skill names `Object` and `dynamic` as ClojureDart's catch-all types, and `cljd.core/ExceptionInfo` as the type that catches `ex-info`. It named Dart's `Exception`, which does not catch `ex-info`.
- The `clojure` skill's money guidance states that ClojureDart compiles `0.1M` to an ordinary double, so the `M` suffix gives no decimal precision there.
- The `clojure` skill's untrusted input reference states that `cljs.reader/read-string` also accepts an options map, and that both of its forms apply every tag parser registered with `register-tag-parser!`.
- `/clj-smells-fix` notes that clj-kondo checks `clojure.lang.RT` calls in `.cljd` files, which it lints as Clojure, and that it reports a reader conditional there as a syntax error. The clj-smells digest labels the blocking `go` examples as JVM code, because ClojureScript core.async has no `<!!`, `>!!`, or `alts!!`.

### Removed

- The `clj-smells-fix` clj-kondo overlay does not name a minimum clj-kondo version.

## [0.1.39] - 2026-10-25

### Added

- The `clojure` skill opens with Iron Laws (CLJ-1 through CLJ-12): numbered rules that prevent bugs, data loss, security holes, and false reports of success. The agent does not write code that breaks a law, and when existing code breaks one, it reports the law ID, the location, and the correct pattern, then asks before changing code outside the task. Key Rules that guarded against bugs or security holes moved into the laws, and the remaining Key Rules are renumbered.
- The `clojure` skill's new `references/untrusted-input.md` covers reading external data with an EDN reader on each host, SQL parameters, and passing shell command arguments separately. Its other references gained examples for lazy sequences inside `with-open`, transients, `contains?` on vectors, `case` with literal constants, money as `BigDecimal` with explicit rounding or as integer minor units, side effects outside `swap!`, side-effect-free namespace loading, macro arguments evaluated at most once, rethrowing `ex-info` with the cause, and allowlisted SQL identifiers.

### Changed

- `clojure-repl` points to Iron Law CLJ-12 for verifying the saved source in a fresh process.

## [0.1.38] - 2026-10-25

### Added

- The `clojure-repl` skill loads when the agent evaluates code in a running REPL. It finds and verifies the project's REPL, prefers clojure-mcp or `clj-nrepl-eval` when available, evaluates small forms with bounded output, asks before evaluating side effects outside the process, and confirms REPL results against the saved source.
- The `clojure-debug` skill loads when code, a build, or a test fails with an error. It keeps the original error output, identifies the error phase, reads the whole cause chain with `ex-message`, `ex-data`, and `ex-cause`, and reproduces the failure before fixing it.
- The `clojure-deps` skill loads when the agent checks a dependency. It reads the resolved version, uses documentation and source for that exact version, and looks up releases on Clojars and Maven Central without treating a prerelease as the latest stable release.
- The `edit-lint` hook lints each Clojure family file the agent edits with clj-kondo and the project's own configuration, and returns new findings to the agent in the same turn, so an unbalanced delimiter or an unresolved symbol is caught before the code runs. It runs under Claude Code, Codex (after you trust the hook), and the GitHub Copilot CLI, needs Babashka and clj-kondo, and never edits a file.

### Changed

- The `clojure` skill loads a short entry file that keeps the Key Rules and routes each task to a reference file: naming and function design, control flow, collections, state and dispatch, macros, namespaces, formatting, and testing. The detailed guidance moved into those files without changes, so less text loads on every matching task and the agent reads the detail for the topic at hand.
- The `clojure` skill points to `clojure-repl`, `clojure-debug`, and `clojure-deps` for REPL, error, and dependency work.

## [0.1.37] - 2026-10-25

### Changed

- The Companion packages table describes biff-skills as covering Biff 2 conventions and deployment, because that package no longer scaffolds projects.

### Removed

- The `/clj-new` scaffolding skill is removed. It wrote a fixed project layout with pinned dependency versions, and only a typed slash command could start it. `/clj-fix` still shows the `:dry4clj` alias to add when a project lacks it.

## [0.1.36] - 2026-10-24

### Added

- The Companion packages table lists [electric-skills](https://github.com/brackendev/electric-skills), the new companion package for the [Electric](https://github.com/hyperfiddle/electric) v3 full-stack framework, and `CONTRIBUTING.md` cites its Hyperfiddle lavender brand color (`#B7A4C2`).
- The `clojure` skill teaches the Clojure 1.11 core functions `update-vals`, `update-keys`, `parse-long`, `parse-double`, `parse-boolean`, `parse-uuid`, `random-uuid`, and `abs`, and the `:as-alias` require option. ClojureScript and ClojureDart provide all of them.

### Changed

- The `clojure` skill lists `electric-skills` among its companion packages.
- The `/clj-fix` description points Electric projects to `/electric-fix` when it is installed, alongside `/cljs-fix`, `/fulcro-fix`, and `/cljd-fix`.
- The Companion packages table names the major version each framework package targets: Biff 2, Electric v3, and Fulcro 3. The skills in those packages do not apply to other major versions.
- The `clojure` skill's companion list names Biff 2 and Fulcro 3.
- `/clj-new` adds a `:dry4clj` alias, so `/clj-fix` runs its duplicate-form step on a new project. When a project has no such alias, `/clj-fix` reports the dry step as `SKIPPED` and shows the alias to add, instead of reporting `ERROR`.
- The `clojure` skill starts a REPL only when the runtime has REPL tooling. Without it, the agent edits files and verifies them with the project's test and lint commands.
- `/clj-fix` lints the source and test paths that `deps.edn`, `project.clj`, or `shadow-cljs.edn` declare, instead of always linting `src` and `test`.
- `/clj-smells-fix` covers `.cljd` files. It imports ClojureDart's clj-kondo configuration first, and it reports, without rewriting, the unresolved-namespace findings that static calls on Dart classes produce.
- `/clj-new` states the date its fixed versions were last verified (2026-10-24).
- `CONTRIBUTING.md` asks for a changelog entry and a version increment for every change to a published file, including documentation-only changes, which matches the release history. It asked for entries only for user-facing changes.

### Fixed

- The `CONTRIBUTING.md` Layout table names the local `AGENTS.md`, `GEMINI.md`, and `TODO.md` files. It named `CLAUDE.md`, which the package does not use.
- `/clj-fix` reads source paths from `deps.edn` when `shadow-cljs.edn` sets `:deps`, because shadow-cljs then ignores its own `:source-paths`.
- The README explains how Codex, OpenCode, and Gemini CLI offer the commands: Codex runs a skill as `$name` from its `/skills` list, OpenCode attaches one with `@name`, and Gemini CLI does not read the user-only setting. It said every command appears as a slash command and that user-only commands run only when typed.
- `CONTRIBUTING.md` states that `agents/openai.yaml` sets `allow_implicit_invocation` to `true` for every skill the model can run, including slash commands that the model can also run. It named only model-invoked and user-only skills.
- `/clj-fix` reports the dry step as `ERROR` when dry4clj stops on an alias-qualified auto-resolved keyword such as `::str/ok`, which the pinned dry4clj commit cannot read, and keeps the step advisory. It said that dry4clj always exits 0.

## [0.1.35] - 2026-10-24

### Fixed

- `/clj-smells-fix` passes its clj-kondo overlay with `--config`. clj-kondo has no `--config-file` option and ignored that spelling without an error, so the overlay never applied.
- `/clj-smells-fix` and `/clj-fix` detect direct `clojure.lang.RT` calls with the `:discouraged-java-method` linter, which requires clj-kondo 2025.06.05 or later. The `:discouraged-var` setting never matched these calls, because `RT` members are Java static methods, not vars.
- `/clj-fix` runs clj-kondo with the `clj-smells-fix` overlay, as its instructions already said it did.
- The overlay takes precedence over the project's clj-kondo levels, so `/clj-smells-fix` and `/clj-fix` skip a linter that the project's `.clj-kondo/config.edn` sets to `:off` and note it in the report.
- `/clj-smells-fix` and `/clj-fix` no longer list nested `when-let` among their automatic fixes. clj-kondo's `:redundant-let` linter flags nested `let` only.
- The `/clj-smells-fix` description of `--report` output matches its report template: the fixes it would apply appear under "Would apply".
- `/clj-fix` and `/clj-smells-fix` add a "skipped N vendored or generated paths" line to the report when the vendored-path filter excludes any path, and list those paths under `--report`, as Rule 4 of `CONVENTIONS.md` requires.

### Changed

- The `clojure` skill allows a blank line between `let` or `cond` pairs that do not fit on one line, as the Clojure style guide does. The `clojure-lenses` Tidy First guidance no longer suggests blank lines anywhere else inside a definition.
- `/clj-smells-fix` states each rule once, and its false-positive guardrails now include the extra cases from the catalog digest.

## [0.1.34] - 2026-10-24

### Removed

- OpenCode 1 is no longer supported. The repository no longer keeps a copy of each `SKILL.md` under `.opencode/skills/` or a per-skill allow list in `opencode.jsonc`. `.opencode/skills` is now a symlink to `.apm/skills/` instead, so an OpenCode 2 session started anywhere in the repository loads the canonical skills together with their supporting files. The `.opencode/package.json` file, which pinned the OpenCode 1 plugin package, is removed as well. APM deploys the same files as before.

### Changed

- The `CONTRIBUTING.md` acceptance steps list the skills OpenCode discovers with `opencode api GET "/api/skill?location[directory]=$PWD"`, because OpenCode 2 has no `--pure` flag or `debug skill` command.

## [0.1.33] - 2026-10-24

### Fixed

- The `CONTRIBUTING.md` acceptance steps install with `apm install --target all` in the clean temporary project. On APM 0.34.0, a bare `apm install` in that project stops with "Multiple harnesses detected" because the steps create several runtime folders first. `apm update` reuses the targets that the install records in the project's `apm.yml`.

## [0.1.32] - 2026-10-17

### Changed

- In Claude Code and Codex, the agent can now run `/clj-fix` and `/clj-smells-fix` from a natural-language request, such as "lint this Clojure project", as well as from the slash command. Their descriptions now state when to use each one. `/clj-new` remains user-only.

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
