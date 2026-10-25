---
name: clj-fix
description: >-
  Fix a Clojure project with lint, format, test, and dry (duplicate-form)
  steps. Applies safe lint fixes and formatting by default. Use when asked to
  lint, format, test, or check a Clojure project, or to fix clj-kondo warnings
  or cljfmt formatting in Clojure code. For ClojureScript, Electric, Fulcro,
  and ClojureDart projects, prefer /cljs-fix, /electric-fix, /fulcro-fix, or
  /cljd-fix when installed. Use --report to preview the lint and format
  changes without writing.
argument-hint: "[lint|format|test|dry] [--report] [all]"
user-invocable: true
---

# Clojure Fix

Run lint, format, test, and duplicate-form checks on Clojure source files. The lint and format steps write by default: lint applies the safe mechanical fixes that `clj-smells-fix` owns, and format rewrites formatting. The test and dry steps are pure-read.

## Arguments

| Input             | Target                                                                       |
|-------------------|------------------------------------------------------------------------------|
| (no argument)     | Run all four steps (`lint`, `format`, `test`, `dry`) on the project            |
| `all`             | Same as (no argument); accepted for family consistency                       |
| `lint`            | Run lint only                                                                |
| `format`          | Run format only                                                              |
| `test`            | Run tests only                                                               |
| `dry`             | Run dry only                                                                 |
| `--report`        | Disable all writes: report the lint fixes, and run `cljfmt check` instead of `cljfmt fix` |

Step keywords are combinable (for example, `/clj-fix lint test`). The `--report` flag may appear in any position. When `--report` is present without an explicit step keyword, every step still runs, and the lint and format steps report their changes instead of writing them.

## Mutation

The `lint` and `format` steps write by default. The `format` step runs `cljfmt fix` through the runner the Format step below selects, rewriting files in place. The `lint` step applies the safe mechanical fix band that `clj-smells-fix` owns, described in the Lint step below. With `--report`, neither step writes: the `format` step runs `cljfmt check` through the same runner, which exits non-zero when files would change but does not write, and the `lint` step reports the fixes it would apply without editing files. The `test` and `dry` steps are pure-read regardless of `--report`.

This skill excludes vendored, generated, and dependency-locked paths from the file set it walks. The filter combines `.gitignore` matches and a hardcoded floor (`node_modules/`, `vendor/`, `third_party/`, `.bundle/`, `target/`, `build/`, `dist/`, `out/`, `.shadow-cljs/`, `cljd-out/`, `*.lock`, `package-lock.json`, `yarn.lock`, `pnpm-lock.yaml`, `Gemfile.lock`, `Cargo.lock`, `poetry.lock`, `composer.lock`). This skill takes no path argument, so the filter applies to every run. When the filter excludes any path, add a single "skipped N vendored or generated paths" line to the report. Under `--report`, list every skipped path. The full policy is Rule 4 in the package's [CONVENTIONS.md](https://github.com/brackendev/clojure-skills/blob/master/CONVENTIONS.md).

## Steps

Parse `$ARGUMENTS` to determine which steps to run and whether `--report` is present. If no step keyword is supplied (or only `all` is supplied), run every step in order.

### 1. Lint

Run clj-kondo on the project's source and test paths with the `clj-smells-fix` overlay, which this package installs next to this skill. Resolve the paths from the build configuration instead of assuming directory names:

- `deps.edn`: the top-level `:paths`, plus the `:extra-paths` of the `:test` alias.
- Leiningen `project.clj`: `:source-paths` and `:test-paths`.
- `shadow-cljs.edn`: `:source-paths`, unless `shadow-cljs.edn` sets `:deps`. With `:deps`, shadow-cljs takes its source paths from `deps.edn`, so use the `deps.edn` paths instead, plus the `:extra-paths` of the aliases that `:deps {:aliases [...]}` names.

Skip resource directories and paths that do not exist. If no configuration declares paths, lint the source and test directories that exist in the repository. Pass the overlay by absolute path, because the working directory is the user's project:

```bash
clj-kondo --lint <source-and-test-paths...> \
  --config <skill-dir>/../clj-smells-fix/references/clj-kondo-overlay.edn
```

Then apply the safe mechanical fix band that `clj-smells-fix` owns: the clj-kondo findings with a deterministic, local rewrite. These are redundant `do` blocks (`:redundant-do`, unwrapped), nested `let` forms (`:redundant-let`, merged into the outer binding vector), `:refer :all` (`:refer-all`, expanded to explicit refers for the symbols the namespace uses), `:use` (`:use`, rewritten as `:require :refer`), unused `:require` entries (`:unused-namespace`, removed), and unused `let` bindings whose initializers have no side effects (`:unused-binding`, removed). Unused function parameters and destructuring bindings are reported, not rewritten. Direct `clojure.lang.RT` usage has no safe general rewrite, so report it with the replacement its message names, without applying it. A symbol used as a `case` test constant (`:case-symbol-test`, which the overlay reports at `:info`) can be deliberate, so report it for review and never rewrite it. Apply the false-positive guardrails the `clj-smells-fix` skill defines, and skip a linter the project's `.clj-kondo/config.edn` sets to `:off` as that skill's Gotchas describe, so the two skills stay in agreement. When this step rewrites a file and the `format` step is not also running, format each rewritten file with the Fix command of the runner that the Format step below selects, passing only those files as paths, so the edits match the project's formatting. When no runner is available, say in the report that the rewritten files were not formatted.

With `--report`, do not write. Instead, list the fixes this step would apply.

Report pass if no `:error` or `:warning` finding remains after the safe fixes are applied, and fail if one remains, because it needs manual attention. List `:info` findings, such as the overlay's `:case-symbol-test`, for review without failing the step. clj-kondo lints `.cljd` files as Clojure and reports a reader conditional there as the error "Reader conditionals are only allowed in .cljc files". ClojureDart reads conditionals in `.cljd` files, so list that finding without counting it as a failure. Show the clj-kondo output and the fixes applied (or, under `--report`, the fixes that would be applied).

### 2. Format

Run cljfmt with the first runner the project supports, checking the rows in order, and check each condition before running its command. Without a `:cljfmt` alias, `clojure -M:cljfmt fix` treats `fix` as a script path and fails with "fix (No such file or directory)", after the warning "Specified aliases are undeclared and are not being used: [:cljfmt]" when the run rebuilds the classpath.

| Runner | When | Fix | Check |
|--------|------|-----|-------|
| Project alias | `deps.edn` defines a `:cljfmt` alias | `clojure -M:cljfmt fix` | `clojure -M:cljfmt check` |
| Leiningen plugin | `project.clj` lists `dev.weavejester/lein-cljfmt` in `:plugins` | `lein cljfmt fix` | `lein cljfmt check` |
| Standalone binary | `command -v cljfmt` succeeds | `cljfmt fix` | `cljfmt check` |
| Clojure CLI tool | `clojure -Ttools list` shows a `cljfmt` tool | `clojure -Tcljfmt fix` | `clojure -Tcljfmt check` |

Without `--report`, run the Fix command. With `--report`, run the Check command, which writes nothing.

Outside Leiningen, cljfmt formats `src`, `test`, and `project.clj` unless its configuration sets `:paths`. When the project's source or test paths differ, pass the paths that the Lint step resolved: as arguments to the alias, the binary, or `lein cljfmt` (for example `clojure -M:cljfmt fix src dev test`), and as an option to the Clojure CLI tool, which takes no positional paths (`clojure -Tcljfmt fix :paths '["src" "dev" "test"]'`). cljfmt stops with "No such file:" when a path does not exist. Without paths, `lein cljfmt` formats the project's `:source-paths` and `:test-paths`. cljfmt's default `:file-pattern` does not match `.cljd`, so it skips ClojureDart files unless the configuration sets one, such as `{:file-pattern #re "\\.(?:clj[csxd]?|bb)$"}` in `.cljfmt.edn`. `/cljd-fix` covers that case.

cljfmt reads its configuration from `.cljfmt.edn` or `cljfmt.edn` in the project directory or a parent. `lein cljfmt` reads the `:cljfmt` map in `project.clj` instead, and reads `.cljfmt.edn` as well only when that map sets `:load-config-file? true`. cljfmt ignores `.cljfmt.clj` and `cljfmt.clj` with a warning unless `--read-clj-config-files` is passed, because it reads those files as Clojure code, which can run code. When the project has only a `.clj` configuration file, ask the user before passing that flag. The alias and the binary accept it, and the Clojure CLI tool never reads a `.clj` configuration file. Never fail a project for the name of its configuration file.

When no runner is available, report `SKIPPED`, show this alias to add under `:aliases` in `deps.edn`, and name the cljfmt README (https://github.com/weavejester/cljfmt) for the other installation methods:

```clojure
:cljfmt {:replace-deps {dev.weavejester/cljfmt {:mvn/version "0.16.6"}}
         :main-opts ["-m" "cljfmt.main"]}
```

`:replace-deps` runs cljfmt without the project's dependencies, which a formatter does not need, so a project dependency that conflicts with cljfmt's own or cannot be resolved does not stop the format step.

Report pass if exit code is 0, fail otherwise. Show any formatting changes (under `fix`) or the diff (under `check`).

### 3. Test

Run the project's own test command:

| Project | Command |
|---------|---------|
| `deps.edn` `:test` alias with `:exec-fn` (Cognitect test-runner) | `clojure -X:test` |
| `deps.edn` `:test` alias with `:main-opts` (for example Kaocha or the test-runner main) | `clojure -M:test` |
| Kaocha binstub | `bin/kaocha` |
| Leiningen | `lein test` |

When the project defines none of these, report `SKIPPED` and say which test configuration is missing. Report pass if exit code is 0, fail otherwise. Show the test output.

### 4. Dry

Scan for duplicate top-level forms with [dry4clj](https://github.com/unclebob/dry4clj). This step is advisory. It reports duplication candidates for review but never fails the run and never halts the pipeline.

Determine the project's production source directories from its build configuration instead of assuming a directory name:

- `deps.edn`: the top-level `:paths` (the `:dry4clj` alias is defined here). Tests usually live under a separate alias's `:extra-paths`, so scanning `:paths` excludes them.
- Leiningen `project.clj`: `:source-paths` (tests live under `:test-paths`).
- `shadow-cljs.edn`: `:source-paths`, unless `shadow-cljs.edn` sets `:deps`. With `:deps`, shadow-cljs takes its source paths from `deps.edn`, so use the `deps.edn` paths instead, plus the `:extra-paths` of the aliases that `:deps {:aliases [...]}` names.

Scan the production source paths only and exclude test paths. If no configuration declares source paths, scan the source directories that exist in the repository, excluding any test directory. Do not assume `src`.

Check that `deps.edn` defines a `:dry4clj` alias before running the scan. When it does not, report `SKIPPED` and show the alias to add under `:aliases`:

```clojure
:dry4clj {:extra-deps {io.github.unclebob/dry4clj {:git/url "https://github.com/unclebob/dry4clj.git"
                                                   :git/sha "5994ef2bcaaae8d12bcd0afb4f091ae09083228c"}}
          :main-opts  ["-m" "dry4clj.core"]}
```

Run dry4clj with EDN output over the resolved paths:

```bash
clojure -M:dry4clj --edn <source-paths...>
```

Do not pass `--threshold`, `--min-lines`, or `--min-nodes`. Scoping to production source removes the dominant noise, which is repeated test scaffolding; tightening the score or size knobs either hides real matches or has no effect on exact-structure duplicates.

dry4clj exits 0 after a completed scan, and the EDN output never prints a clean-state message, so do not use the exit code or any text match to detect duplicates. A non-zero exit means the scan itself failed (for example, a file the reader cannot parse) and is reported as `ERROR`. Parse the EDN map `{:candidates [...]}` and classify the result:

| Result   | Condition                                                                                                                  |
|----------|----------------------------------------------------------------------------------------------------------------------------|
| `PASS`   | `:candidates` is empty.                                                                                                     |
| `REVIEW` | `:candidates` has one or more entries. List them and continue.                                                             |
| `ERROR`  | The command fails or its output cannot be parsed. Report the cause, and do not report `PASS`. |
| `SKIPPED` | No production source path can be identified, or `deps.edn` defines no `:dry4clj` alias. |

dry4clj reads source without resolving namespace aliases, so an alias-qualified auto-resolved keyword such as `::str/ok` stops the scan with "Invalid token" and a non-zero exit. Plain auto-resolved keywords such as `::ok` read correctly. Report the file and the error as `ERROR`, keep the step advisory, and do not rewrite source to work around the reader.

For `REVIEW`, sort candidates by exact matches first (`:score` equal to `1.0`), then by descending `min(:left-nodes, :right-nodes)`, then by descending `:score`. Report the scanned source paths, the candidate count, and the highest-priority candidates with their score, node counts, and both file ranges. Note that each candidate needs source inspection before extraction, and that test directories were excluded because repeated test scaffolding is often intentional.

## Report

After running all requested steps, print a summary:

```
Clojure Fix Results:
  Lint:   PASS/FAIL/SKIPPED
  Format: PASS/FAIL/SKIPPED
  Test:   PASS/FAIL/SKIPPED
  Dry:    PASS/REVIEW/ERROR/SKIPPED
```

If `--report` was passed, append `(report mode: lint and format changes reported, not written)` after the summary. If a lint, format, or test step fails, stop and report the failure; do not continue to subsequent steps.

## Gotchas

- A regular expression in `.cljfmt.edn`, such as an `:extra-indents` key or a `:file-pattern`, must use the `#re` tag: `{:extra-indents {#re ".*" [[:inner 0]]}}`. `#".*"` is not valid EDN, and cljfmt stops with "No dispatch macro for: \"" before it formats anything. When the project's file uses `#"`, report the format step as failed, show the `#re` form of each expression, and do not edit the configuration file without the user's confirmation.
