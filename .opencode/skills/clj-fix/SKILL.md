---
name: clj-fix
description: "Fix a Clojure project (lint, format, test, dry); writes formatting by default"
argument-hint: "[lint|format|test|dry] [--report] [all]"
user-invocable: true
disable-model-invocation: true
---

# Clojure Fix

Run lint, format, test, and duplicate-form checks on Clojure source files. The format step writes by default; the other three steps are pure-read.

## Arguments

| Input             | Target                                                                       |
|-------------------|------------------------------------------------------------------------------|
| (no argument)     | Run all four steps (`lint`, `format`, `test`, `dry`) on the project            |
| `all`             | Same as (no argument); accepted for family consistency                       |
| `lint`            | Run lint only                                                                |
| `format`          | Run format only                                                              |
| `test`            | Run tests only                                                               |
| `dry`             | Run dry only                                                                 |
| `--report`        | Replace `cljfmt fix` with non-writing `cljfmt check` in the format step      |

Step keywords are combinable (for example, `/clj-fix lint test`). The `--report` flag may appear in any position. When `--report` is present without an explicit step keyword, every step still runs; only the format step's behavior changes.

## Mutation

Only the `format` step writes. It runs `clj -M:cljfmt fix` by default, rewriting files in place. With `--report`, the step runs `clj -M:cljfmt check`, which exits non-zero when files would change but does not write. The `lint`, `test`, and `dry` steps are pure-read regardless of `--report`.

This skill excludes vendored, generated, and dependency-locked paths from the file set it walks. The filter combines `.gitignore` matches and a hardcoded floor (`node_modules/`, `vendor/`, `third_party/`, `.bundle/`, `target/`, `build/`, `dist/`, `out/`, `.shadow-cljs/`, `cljd-out/`, `*.lock`, `package-lock.json`, `yarn.lock`, `pnpm-lock.yaml`, `Gemfile.lock`, `Cargo.lock`, `poetry.lock`, `composer.lock`). Naming a path directly via `<path>` or `<glob>` bypasses the filter for that target; broad scopes (`(no argument)`, `all`, or a parent directory) keep the filter active. The full policy is Rule 4 in CONVENTIONS.md.

## Steps

Parse `$ARGUMENTS` to determine which steps to run and whether `--report` is present. If no step keyword is supplied (or only `all` is supplied), run every step in order.

### 1. Lint

Run clj-kondo on the Clojure source:

```bash
clj-kondo --lint src test
```

Report pass if exit code is 0, fail otherwise. Show the clj-kondo output.

### 2. Format

Without `--report`, run cljfmt to fix formatting:

```bash
clj -M:cljfmt fix
```

With `--report`, run cljfmt in check mode (no writes):

```bash
clj -M:cljfmt check
```

Report pass if exit code is 0, fail otherwise. Show any formatting changes (under `fix`) or the diff (under `check`).

### 3. Test

Run the test suite:

```bash
clj -X:test
```

Report pass if exit code is 0, fail otherwise. Show the test output.

### 4. Dry

Scan for duplicate top-level forms with [dry4clj](https://github.com/brackendev/dry4clj). This step is advisory. It reports duplication candidates for review but never fails the run and never halts the pipeline.

Determine the project's production source directories from its build configuration instead of assuming a directory name:

- `deps.edn`: the top-level `:paths` (the `:dry4clj` alias is defined here). Tests usually live under a separate alias's `:extra-paths`, so scanning `:paths` excludes them.
- Leiningen `project.clj`: `:source-paths` (tests live under `:test-paths`).
- `shadow-cljs.edn`: `:source-paths`.

Scan the production source paths only and exclude test paths. If no configuration declares source paths, scan the source directories that exist in the repository, excluding any test directory. Do not assume `src`.

Run dry4clj with EDN output over the resolved paths:

```bash
clj -M:dry4clj --edn <source-paths...>
```

Do not pass `--threshold`, `--min-lines`, or `--min-nodes`. Scoping to production source removes the dominant noise, which is repeated test scaffolding; tightening the score or size knobs either hides real matches or has no effect on exact-structure duplicates.

dry4clj always exits 0, and the EDN output never prints a clean-state message, so do not use the exit code or any text match as the signal. Parse the EDN map `{:candidates [...]}` and classify the result:

| Result   | Condition                                                                                                                  |
|----------|----------------------------------------------------------------------------------------------------------------------------|
| `PASS`   | `:candidates` is empty.                                                                                                     |
| `REVIEW` | `:candidates` has one or more entries. List them and continue.                                                             |
| `ERROR`  | The command cannot run or its output cannot be parsed (for example, the `:dry4clj` alias is missing). Report the cause; do not report `PASS`. |
| `SKIP`   | No production source path can be identified.                                                                               |

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

If `--report` was passed, append `(report mode: format checked, not written)` after the summary. If a lint, format, or test step fails, stop and report the failure; do not continue to subsequent steps. The dry step is advisory: it never fails the run and never halts the pipeline.
