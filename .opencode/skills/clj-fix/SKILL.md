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
| (no argument)     | Run all four steps (`lint`, `format`, `test`, `dry`) on the project (src, test) |
| `all`             | Same as (no argument); accepted for family consistency                       |
| `lint`            | Run lint only                                                                |
| `format`          | Run format only                                                              |
| `test`            | Run tests only                                                               |
| `dry`             | Run dry only                                                                 |
| `--report`        | Replace `cljfmt fix` with non-writing `cljfmt check` in the format step      |

Step keywords are combinable (for example, `/clj-fix lint test`). The `--report` flag may appear in any position. When `--report` is present without an explicit step keyword, every step still runs; only the format step's behavior changes.

## Mutation

Only the `format` step writes. It runs `clj -M:cljfmt fix` by default, rewriting files in place. With `--report`, the step runs `clj -M:cljfmt check`, which exits non-zero when files would change but does not write. The `lint`, `test`, and `dry` steps are pure-read regardless of `--report`.

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

Scan for duplicate top-level forms with [dry4clj](https://github.com/unclebob/dry4clj):

```bash
clj -M:dry4clj src test
```

If the project has no `test/` directory, scan `src` only.

dry4clj exits 0 whether or not it finds candidates, so the step must inspect output. Report pass only when exit code is 0 and stdout contains the literal `No duplicate candidates found.`. Otherwise report fail and show the reported candidates. The project's `deps.edn` must define a `:dry4clj` alias; if the alias is missing, the Clojure CLI exits non-zero and the step fails.

## Report

After running all requested steps, print a summary:

```
Clojure Fix Results:
  Lint:   PASS/FAIL/SKIPPED
  Format: PASS/FAIL/SKIPPED
  Test:   PASS/FAIL/SKIPPED
  Dry:    PASS/FAIL/SKIPPED
```

If `--report` was passed, append `(report mode: format checked, not written)` after the summary. If any step fails, stop and report the failure. Do not continue to subsequent steps.
