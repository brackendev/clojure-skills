---
name: cljd-check
description: Run the ClojureDart quality pipeline (lint, format, compile)
argument-hint: "[lint|format|compile]"
user-invocable: true
disable-model-invocation: true
---

# ClojureDart Quality Check

Run lint, format, and compile checks on ClojureDart source files.

## Steps

Parse `$ARGUMENTS` to determine which steps to run. If empty, run all steps in order.

| Argument | Steps |
|----------|-------|
| (empty) | lint, format, compile |
| `lint` | lint only |
| `format` | format only |
| `compile` | compile only |

Multiple arguments can be combined (e.g., `lint compile`).

### 1. Lint

Run clj-kondo on the ClojureDart source:

```bash
clj-kondo --lint src
```

Report pass if exit code is 0, fail otherwise. Show the clj-kondo output.

### 2. Format

Run cljfmt to fix formatting:

```bash
clj -M:cljfmt fix
```

Report pass if exit code is 0, fail otherwise. Show any formatting changes.

### 3. Compile

Compile ClojureDart to Dart:

```bash
clj -M:cljd compile
```

Report fail if exit code is non-zero, or if the output contains any `DYNAMIC WARNING: can't resolve member` lines. Those resolution failures exit 0 but indicate a method or property that does not exist on the target type, which throws `NoSuchMethodError` at runtime. Show any compilation errors and the offending warning lines.

## Report

After running all requested steps, print a summary:

```
ClojureDart Check Results:
  Lint:    PASS/FAIL/SKIPPED
  Format:  PASS/FAIL/SKIPPED
  Compile: PASS/FAIL/SKIPPED
```

If any step fails, stop and report the failure. Do not continue to subsequent steps.
