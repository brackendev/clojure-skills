---
name: clj-smells-fix
description: >-
  Fix Clojure code against the clj-smells catalog of Clojure-specific code
  smells. Auto-applies Stage 1 mechanical findings and Stage 2 DEFECT-tier
  findings, and reports SMELL and HINT findings. Use when asked to find or fix
  Clojure code smells, or to fix namespace problems such as `:refer :all`,
  `:use`, or unused requires. Use --report to disable writes.
argument-hint: "[path|all] [--report]"
allowed-tools: Bash, Read, Edit, Grep, Glob
user-invocable: true
---

# Clojure Smells Fix

Review Clojure code against the [clj-smells catalog](https://github.com/nufuturo-ufcg/clj-smells-catalog), a community catalog of 35 Clojure-specific code smells with descriptions, examples, and source citations. The skill pairs `clj-kondo` static analysis with LLM-assisted detection for smells static analysis cannot catch, then applies the safe subset of findings to source files.

## Catalog Source

Pinned to upstream commit [`d1ae189`](https://github.com/nufuturo-ufcg/clj-smells-catalog/tree/d1ae1896f518e94f3d4f9257b451d89473b94e1b) (2026-01-19). The smell digest in `references/clj-smells-catalog.md` reflects this snapshot. Update both the pin and the digest when upstream revises the catalog.

## Arguments

| Input              | Target                                                                       |
|--------------------|------------------------------------------------------------------------------|
| (no argument)      | Fix changed files only (staged + unstaged)                                   |
| `all`              | Fix the full codebase, sampling high-risk and high-traffic namespaces        |
| `path/to/dir`      | Fix files under directory                                                    |
| `path/to/file.clj` | Fix specific file                                                            |
| `--report`         | Disable all writes; produce the report only                                  |

Examples:

```
/clj-smells-fix
/clj-smells-fix src/api
/clj-smells-fix src/api/auth.clj
/clj-smells-fix all
/clj-smells-fix --report
/clj-smells-fix src/api --report
```

The `--report` flag may appear in any position.

## Mutation

The skill applies findings in two narrow bands. Everything else is reported.

**Auto-fixed by default:**

- **Stage 1 mechanical findings** from the `clj-kondo` overlay: redundant `do` blocks (`:redundant-do`, unwrapped), nested `let` forms (`:redundant-let`, merged into the outer binding vector), `:refer :all` (`:refer-all`, expanded to explicit refers for the symbols the namespace actually uses), `:use` (`:use`, rewritten as `:require :refer`), unused `:require` entries (`:unused-namespace`, removed), and unused `let` bindings whose initializers have no side effects (`:unused-binding`, removed). Unused function parameters and destructuring bindings are reported, not rewritten.
- **Stage 2 DEFECT-tier findings** with a local, well-defined rewrite: macro double-evaluation (bind the argument once in a `let`), unwrapped resource handles (wrap in `with-open` when the JVM `clojure.java.io` type is in scope), blocking forms inside `go` blocks (rewrite `<!!`/`>!!` to `<!`/`>!` when the surrounding form already provides park semantics), load-time side effects inside `def` bodies when every caller is in scope and can be rewritten in the same pass (wrap the right-hand side in `delay` and update every caller to dereference it with `@` or `deref`).

**Reported only, never auto-applied:**

- Stage 1 finding: direct `clojure.lang.RT` usage (`:discouraged-java-method`). No safe general rewrite exists; the skill reports the call sites and suggested replacements.
- Stage 2 `SMELL` and `HINT` findings: maintainability or style improvements that depend on intent.
- Any Stage 2 `DEFECT` finding outside the list above, where the fix would require changing call sites across the codebase, changing public API shape, or introducing a new dependency. The report describes the suggested change.

With `--report`, the skill produces the same report but writes nothing. Every finding appears as a suggestion. The fixes it would otherwise apply automatically are listed as deferred suggestions in a "Would apply" section, which takes the place of "Applied fixes".

This skill excludes vendored, generated, and dependency-locked paths from the file set it walks. The filter combines `.gitignore` matches and a hardcoded floor (`node_modules/`, `vendor/`, `third_party/`, `.bundle/`, `target/`, `build/`, `dist/`, `out/`, `.shadow-cljs/`, `cljd-out/`, `*.lock`, `package-lock.json`, `yarn.lock`, `pnpm-lock.yaml`, `Gemfile.lock`, `Cargo.lock`, `poetry.lock`, `composer.lock`). Naming a vendored path directly through `<path>` or `<glob>` bypasses the filter for that target. When the filter excludes any path, add a single "skipped N vendored or generated paths" line to the report. Under `--report`, list every skipped path. The full policy is Rule 4 in the package's [CONVENTIONS.md](https://github.com/brackendev/clojure-skills/blob/master/CONVENTIONS.md).

## Severity Tiers

| Tier | Meaning |
|------|---------|
| `DEFECT` | Correctness, resource leak, race condition, macro double-evaluation, blocking inside `go`, load-time side effects |
| `SMELL` | Maintainability or design cost with concrete evidence |
| `HINT` | Local idiom, readability, or style improvement |

A single smell name (for example "Production `doall`") can land in different tiers depending on context. Assign tier based on impact, not by smell name alone. Default tier guidance is in `references/clj-smells-catalog.md`.

## Fix Workflow

Run all stages for every invocation.

### 1. Determine Scope

Parse `$ARGUMENTS` for `--report` (any position) and for a scope keyword or path.

Use Bash for changed-file discovery:

- `git status --short`
- `git diff --name-only`
- `git diff --cached --name-only`

Filter to `.clj`, `.cljc`, and `.cljs` files. Apply the path filter described in the Mutation section, and also skip resource directories. If the user provided no scope and no files changed, ask what to fix rather than widening silently to `all`.

Build a scope summary (for example `changed files: src/api.clj, src/db.clj`) for the report header.

### 2. Stage 1: clj-kondo First Pass

Check whether `clj-kondo` is available on PATH:

```bash
command -v clj-kondo
```

If clj-kondo is missing, record a warning in the report (`Stage 1 skipped: clj-kondo not on PATH`) and proceed to Stage 2.

If available, run with the overlay configuration:

```bash
clj-kondo --lint <scope> \
  --config <skill-dir>/references/clj-kondo-overlay.edn \
  --config '{:output {:format :json}}'
```

Parse the JSON output. Each clj-kondo finding maps to a catalog smell as documented in `references/clj-smells-catalog.md`. Promote verified findings to the report with `clj-kondo` as evidence.

clj-kondo (with overlay) detects the Stage 1 findings. Unless `--report` is set, apply via Edit the rewrite that the Mutation section lists for each finding's linter. Send the findings that the Mutation section marks as reported only, such as direct `clojure.lang.RT` usage, to the suggestions list.

Record each applied edit in the report under "Applied fixes" with `file:line` and a one-line summary.

### 3. Stage 2: LLM Pass

For each file in scope, evaluate against the smells in `references/clj-smells-catalog.md` that clj-kondo cannot detect. Apply these rules:

- Read the file (or diff hunk when scoped to changes).
- For each candidate smell, verify the pattern against the actual code, not assumptions.
- Skip smells that are intentional in context (see False-Positive Guardrails below).
- Every finding requires a `file:line` reference and a concrete code excerpt.
- Prefer fewer high-confidence findings over many weak ones.

Classify each finding by tier and by whether it falls within the auto-fix band described in the Mutation section. Apply each `DEFECT`-tier finding within the band via Edit unless `--report` is set. Record applied edits in the report. Defer everything else (`DEFECT` findings outside the band, all `SMELL` findings, all `HINT` findings) to the report as suggestions.

### 4. Aggregate and Report

Use this structure unless the user asked for a shorter variant:

```markdown
## Clojure Smells Fix: [scope]

### Applied fixes

**[file:line]** -- [smell name]
[one-line description of the rewrite]

(under --report, title this section "Would apply" and list the same entries as deferred suggestions; omit the section when no fixes apply)

### Suggestions

**[file:line]** -- [DEFECT|SMELL|HINT] [smell name]
[evidence: short code excerpt or clj-kondo line]
[why it matters in this context]
[suggested fix]

### Verdict

- **Smell density:** None / Low / Moderate / High
- **Detection coverage:** clj-kondo: [available|missing], LLM pass: [N files reviewed]
- **One-sentence summary:** [single sentence]

| Metric | Count |
|--------|-------|
| Files reviewed | N |
| Applied fixes | N |
| DEFECT (reported) | N |
| SMELL | N |
| HINT | N |
| clj-kondo findings | N |
```

If no smells found, say so explicitly and still emit the metric table.

## False-Positive Guardrails

- **`doall` in REPL utilities, one-off scripts, or small bounded sequences** is the explicit intent. Only flag (and only consider auto-fixing) `doall` in production code paths where laziness was the appropriate default.
- **`with-redefs` in tests** is the canonical Clojure seam for stubbing boundaries. Do not flag it as "Misuse of Dynamic Scope" inside a test namespace.
- **`:refer :all` for `clojure.test`** is project convention in many Clojure codebases. Auto-fix only when the project's other test files use `:refer` with explicit symbols.
- **`core.async` channels** are appropriate when the use case requires fan-out, batching, backpressure, or long-running pipelines. Flag "Overengineering with core.async" only when a `promise`, `future`, or plain function would suffice, and never auto-fix this finding.
- **`Refs in Dependency Vector`** is a Reagent/re-frame-specific smell. Do not flag it outside Reagent contexts, including in plain Clojure and non-reactive ClojureScript.
- **Macros** that capture forms, control evaluation order, or generate top-level definitions cannot be replaced with functions. Flag "Unnecessary Macros" only when the macro body is a straightforward expression a function would compute, and never auto-fix it.
- **`:refer :all` of `clojure.core`** is implicit and never a finding.
- Mark uncertain findings as uncertain. Do not auto-fix any finding the model is uncertain about; route it to the suggestions list instead.

## Gotchas

- `--config` merges the overlay over the project's `.clj-kondo/config.edn`, so the overlay's levels take precedence, and a linter the project sets to `:off` (for example `:linters {:redundant-do {:level :off}}`) reports findings again. Treat such a linter as disabled: do not apply or report its findings, and note in the report that the project config disables it.
- When `clj-kondo` exits non-zero, output is still on stdout. Parse stdout regardless of exit code; non-zero exit is expected when findings exist.
- `--config` accepts a file path or an EDN expression, and clj-kondo merges repeated `--config` options. Pass the overlay by absolute path, because the working directory at fix time is the user's project, not the skill directory. clj-kondo has no `--config-file` option and ignores it without an error, so that spelling silently runs without the overlay.
- ClojureScript (`.cljs`) files trigger different clj-kondo behavior. The overlay is JVM-Clojure focused; some smells (for example atoms-in-atoms) apply equally, others (for example RT usage) do not appear in CLJS code.
- `:refer :all` expansion requires reading every symbol the namespace actually uses. When the namespace references symbols through reader-conditional branches, macros, or `eval`, the expansion may miss entries. In that case, route the finding to suggestions rather than auto-fixing.
- Auto-fixes that touch a `:require` form must preserve the rest of the form (other entries, metadata, comments). Re-format with the project's `cljfmt` configuration after the rewrite to avoid drift.
