---
name: clj-check
description: Run the Clojure quality pipeline (lint, format, test)
argument-hint: "[lint|format|test]"
user-invocable: true
disable-model-invocation: true
---

# Clojure Quality Check

Run lint, format, and test checks on Clojure source files.

## Steps

Parse `$ARGUMENTS` to determine which steps to run. If empty, run all steps in order.

| Argument | Steps |
|----------|-------|
| (empty) | lint, format, test |
| `lint` | lint only |
| `format` | format only |
| `test` | test only |

Multiple arguments can be combined (e.g., `lint test`).

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

## Report

After running all requested steps, print a summary:

```
Clojure Check Results:
  Lint:   PASS/FAIL/SKIPPED
  Format: PASS/FAIL/SKIPPED
  Test:   PASS/FAIL/SKIPPED
```

If any step fails, stop and report the failure. Do not continue to subsequent steps.
