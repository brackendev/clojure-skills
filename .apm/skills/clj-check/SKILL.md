---
name: clj-check
description: Run the Clojure quality pipeline (lint, format, test, dry)
argument-hint: "[lint|format|test|dry]"
user-invocable: true
disable-model-invocation: true
---

# Clojure Quality Check

Run lint, format, test, and duplicate-form checks on Clojure source files.

## Steps

Parse `$ARGUMENTS` to determine which steps to run. If empty, run all steps in order.

| Argument | Steps |
|----------|-------|
| (empty) | lint, format, test, dry |
| `lint` | lint only |
| `format` | format only |
| `test` | test only |
| `dry` | dry only |

Multiple arguments can be combined (e.g., `lint test dry`).

### 1. Lint

Run clj-kondo on the Clojure source:

```bash
clj-kondo --lint src test
```

Report pass if exit code is 0, fail otherwise. Show the clj-kondo output.

### 2. Format

Run cljfmt to fix formatting:

```bash
clj -M:cljfmt fix
```

Report pass if exit code is 0, fail otherwise. Show any formatting changes.

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
Clojure Check Results:
  Lint:   PASS/FAIL/SKIPPED
  Format: PASS/FAIL/SKIPPED
  Test:   PASS/FAIL/SKIPPED
  Dry:    PASS/FAIL/SKIPPED
```

If any step fails, stop and report the failure. Do not continue to subsequent steps.
