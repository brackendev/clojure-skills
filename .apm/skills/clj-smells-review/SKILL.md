---
name: clj-smells-review
description: Review Clojure code against the clj-smells catalog (35 Clojure-specific smells); pure report, never writes
argument-hint: "[path|all]"
allowed-tools: Bash, Read, Grep, Glob
user-invocable: true
disable-model-invocation: true
---

# Clojure Smells Review

Review Clojure code against the [clj-smells catalog](https://github.com/nufuturo-ufcg/clj-smells-catalog), a community catalog of 35 Clojure-specific code smells with descriptions, examples, and source citations. This review pairs `clj-kondo` static analysis with LLM-assisted detection for smells that static analysis cannot catch.

## Catalog Source

Pinned to upstream commit [`d1ae189`](https://github.com/nufuturo-ufcg/clj-smells-catalog/tree/d1ae1896f518e94f3d4f9257b451d89473b94e1b) (2026-01-19). The smell digest in `references/clj-smells-catalog.md` reflects this snapshot. Update both the pin and the digest when upstream revises the catalog.

## Arguments

| Input              | Target                                                                       |
|--------------------|------------------------------------------------------------------------------|
| (no argument)      | Review changed files only (staged + unstaged)                                |
| `all`              | Review the full codebase, sampling high-risk and high-traffic namespaces     |
| `path/to/dir`      | Review files under directory                                                 |
| `path/to/file.clj` | Review specific file                                                         |

Examples:

```
/clj-smells-review
/clj-smells-review src/api
/clj-smells-review src/api/auth.clj
/clj-smells-review all
```

This skill is pure-report: it never writes. Operators apply suggestions themselves.

## Severity Tiers

| Tier | Meaning |
|------|---------|
| `DEFECT` | Correctness, resource leak, race condition, macro double-evaluation, blocking inside `go`, load-time side effects |
| `SMELL` | Maintainability or design cost with concrete evidence |
| `HINT` | Local idiom, readability, or style improvement |

A single smell name (e.g. "Production `doall`") can land in different tiers depending on context. Assign tier based on impact, not by smell name alone. Default tier guidance is in `references/clj-smells-catalog.md`.

## Review Workflow

Run all stages for every review.

### 1. Determine Scope

Use Bash for changed-file discovery:

- `git status --short`
- `git diff --name-only`
- `git diff --cached --name-only`

Filter to `.clj`, `.cljc`, and `.cljs` files. Skip generated files, vendored dependencies, and resources. If the user provided no scope and no files changed, ask what to review.

Build a scope summary (e.g. `changed files: src/api.clj, src/db.clj`) for the report header.

### 2. Stage 1 -- clj-kondo First Pass

Check whether `clj-kondo` is available on PATH:

```bash
command -v clj-kondo
```

If clj-kondo is missing, record a warning in the report (`Stage 1 skipped: clj-kondo not on PATH`) and proceed to Stage 2.

If available, run with the overlay configuration:

```bash
clj-kondo --lint <scope> \
  --config-file <skill-dir>/references/clj-kondo-overlay.edn \
  --config '{:output {:format :json}}'
```

Parse the JSON output. Each clj-kondo finding maps to a catalog smell as documented in `references/clj-smells-catalog.md`. Promote verified findings to the report with `clj-kondo` as evidence.

clj-kondo (with overlay) detects:

- Implicit Namespace Dependencies (`:refer :all`)
- Excessive Refers
- Redundant `do` block
- Nested Forms (nested `let`/`when-let`)
- Direct usage of `clojure.lang.RT`

### 3. Stage 2 -- LLM Pass

For each file in scope, evaluate against the smells in `references/clj-smells-catalog.md` that clj-kondo cannot detect. Apply these rules:

- Read the file (or diff hunk when scoped to changes).
- For each candidate smell, verify the pattern against the actual code, not assumptions.
- Skip smells that are intentional in context (see False-Positive Guardrails below).
- Every finding requires a `file:line` reference and a concrete code excerpt.
- Prefer fewer high-confidence findings over many weak ones.

### 4. Aggregate and Report

Use this structure unless the user asked for a shorter variant:

```markdown
## Clojure Smells Review: [scope]

### Findings

**[file:line]** -- [DEFECT|SMELL|HINT] [smell name]
[evidence: short code excerpt or clj-kondo line]
[why it matters in this context]
[suggested fix]

### Verdict

- **Smell density:** None / Low / Moderate / High
- **Detection coverage:** clj-kondo: [available|missing], LLM pass: [N files reviewed]
- **One-sentence summary:** [single sentence]

### Suggestions

1. [highest-impact fix]
2. [next fix]
3. [next fix]

| Metric | Count |
|--------|-------|
| Files reviewed | N |
| DEFECT | N |
| SMELL | N |
| HINT | N |
| clj-kondo findings | N |
```

If no smells found, say so explicitly and still emit the metric table.

## False-Positive Guardrails

- **`doall` in REPL utilities or one-off scripts** is the explicit intent. Only flag `doall` in production code paths where laziness was the appropriate default.
- **`with-redefs` in tests** is the canonical Clojure seam for stubbing boundaries. Do not flag it as "Misuse of Dynamic Scope" inside a test namespace.
- **`:refer :all` for `clojure.test`** is project convention in many Clojure codebases. Flag only when the project's other test files use `:refer` with explicit symbols.
- **`core.async` channels** are appropriate when the use case requires fan-out, backpressure, or long-running pipelines. Flag "Overengineering with core.async" only when a `promise`, `future`, or plain function would suffice.
- **`Refs in Dependency Vector`** is a Reagent/re-frame-specific smell. Do not flag it outside Reagent contexts.
- **Macros** that capture forms, control evaluation order, or generate top-level definitions cannot be replaced with functions. Flag "Unnecessary Macros" only when the macro body is a straightforward expression a function would compute.
- **`:refer :all` of `clojure.core`** is implicit and never a finding.
- Mark uncertain findings as uncertain. Do not claim certainty without evidence from code.

## Gotchas

- The clj-kondo overlay enables linters that may already be configured in the project's `.clj-kondo/config.edn`. The `--config-file` argument supplements project config; it does not override. If a project sets `:linters {:redundant-do {:level :off}}`, the overlay will not re-enable it. Document this in the report when noticed.
- When `clj-kondo` exits non-zero, output is still on stdout. Parse stdout regardless of exit code; non-zero exit is expected when findings exist.
- The `--config-file` flag accepts a single EDN file. Use absolute path resolution since the working directory at review time is the user's project, not the skill directory.
- ClojureScript (`.cljs`) files trigger different clj-kondo behavior. The overlay is JVM-Clojure focused; some smells (e.g. atoms-in-atoms) apply equally, others (e.g. RT usage) do not appear in CLJS code.
