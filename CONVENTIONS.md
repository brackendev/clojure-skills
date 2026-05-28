# Skill Conventions

This document defines the argument grammar, scope vocabulary, and mutation defaults that every user-invocable skill in `clojure-skills` follows. A reader who learns one skill should be able to predict every other skill.

These conventions apply to user-invocable skills (`user-invocable: true` in frontmatter). Model-invocable reference skills with no argument surface (currently `clojure` and `clojure-lenses`) are outside the standard.

## The four rules

### Rule 1: argument grammar

User-invocable skills accept natural-language keywords and bare paths. The single sanctioned flag is `--report`. No other `--name` flags exist.

Skill-specific modifiers are bare phrases, not flags. For example, `lint test dry` (per-step keywords for `/clj-fix`) or `all` (scope keyword). Each skill documents its own modifiers in its `## Arguments` table.

Exemptions are listed in the [Exemptions](#exemptions) section with a reason. The standard exemption pattern is a prompt-template or scaffolding skill that needs a required positional argument because no useful default exists.

### Rule 2: scope vocabulary

Skills that operate on files, diffs, pull requests, or commit messages share these core rows in their `## Arguments` table:

| Input             | Target                                                          |
|-------------------|-----------------------------------------------------------------|
| (no argument)     | The skill's narrowest useful default                            |
| `all`             | Widen the selected scope to its maximum                         |
| `<path>` `<glob>` | Operate on those files or directories                           |

Each skill states what `all` resolves to in concrete terms (full codebase, every PR, the whole project). The keyword has one uniform meaning: widen the selected scope to the maximum. The unit varies per skill.

A skill whose narrowest useful default is already maximum scope still accepts `all` for family consistency. The table row reads "Same as (no argument); accepted for family consistency."

Opt-in rows (`#N` or PR URL, `pr`, `commit`) appear only when the skill genuinely supports them.

### Rule 3: mutation is the default

Skills that can mutate the workspace apply changes when invoked. The operator passes `--report` to receive a description of what the skill would do, without modifying any files.

Only the literal token `--report` enables report-only mode. Natural-language phrases ("preview", "dry run", "rehearse") are scope input or step keywords, not mode triggers.

Command suffixes reinforce the default. The family follows a noun-first `<target>-<verb>` pattern, so the trailing verb signals behavior. Skills with suffix `-fix`, `-sync`, `-prune`, `-rebuild`, `-new`, `-deploy`, `-upgrade`, `-test`, `-create`, `-apply` mutate by default. Skills with suffix `-review`, `-audit`, `-check` are pure-report. Bare verbs `/commit` and `/pause` are session-scoped exceptions that mutate by default.

### Rule 4: vendored and generated paths are excluded by default

A mutating skill that walks the workspace excludes vendored, generated, and dependency-locked paths from its scope. The operator opts back in per file by naming the path explicitly. No new `--name` flag is introduced; the override rides on Rule 1's `<path>` `<glob>` row.

The boundary statement: Rule 4 applies to mutating skills that discover candidate files from the workspace. It does not apply to skills whose target set is defined by an explicit project operation, template, dependency model, git operation, or named path argument.

**Exclusion set.** Two filters apply together. A path that matches either filter is excluded.

1. `.gitignore`-matched paths. Anything excluded by the project's `.gitignore`, `.git/info/exclude`, or the global excludes file is out of scope. Resolve membership with `git check-ignore -v -- <path>`.
2. Hardcoded floor (excluded even when the project tracks the path):

   | Category | Patterns |
   |----------|----------|
   | Dependency directories | `node_modules/`, `vendor/`, `third_party/`, `.bundle/` |
   | Build outputs | `target/`, `build/`, `dist/`, `out/`, `.shadow-cljs/`, `cljd-out/` |
   | Lock files | `*.lock`, `package-lock.json`, `yarn.lock`, `pnpm-lock.yaml`, `Gemfile.lock`, `Cargo.lock`, `poetry.lock`, `composer.lock` |

**Override.** When the operator names a vendored or generated file in the arguments through the `<path>` `<glob>` row, the filter does not apply to that target. Naming `vendor/foo.clj` directly is treated as informed consent. The filter remains active for broad scopes: `(no argument)`, `all`, or a directory whose contents include vendored sub-paths.

**Reporting.** The skill includes a single "skipped N vendored or generated paths" line in its results when the filter excluded any path. Under `--report`, the skill emits the full list so the operator can audit scope.

**Scope of Rule 4 in this plugin.** Applies to `/clj-fix` and `/clj-smells-fix`, both of which discover candidate files from the workspace. Exempt: `/clj-new` is scaffolding (its target set is the new project tree, not workspace discovery). The model-invocable `clojure` and `clojure-lenses` skills have no mutation surface and are also out of scope.

The file-aware mutating skills include a one-line reference to Rule 4 in their own `## Mutation` or `## Scope` section.

## Classification

Every user-invocable skill falls into one of two classes.

**Mutating skill** -- default behavior. May carry `--report` when preview is useful. May omit `--report` when preview is meaningless (the operator inspects `git diff` after the fact) or the action is small and reversible.

**Pure report** -- never mutates. No `--report` flag because there is nothing to invert.

A skill is classified by its actual behavior, not by its name. If the name and behavior disagree, the rename is the fix.

## Section structure

User-invocable skills with an argument surface use these section conventions:

- `## Arguments` -- required. Contains the canonical scope table.
- `## Scope` -- optional. Add only when detection order, fallback, or base-branch resolution exceeds what the table can express.
- `## Mutation` -- optional. Add only when mutation behavior needs clarification beyond a single table row (for example, when only one of several steps writes).

The section name `## Customization` is retired.

## Worked examples

### `/clj-fix` -- mutating skill with `--report`

```
/clj-fix                    # all four steps; format writes
/clj-fix lint               # lint only (pure-read; no writes anywhere)
/clj-fix format             # format step; writes via cljfmt fix
/clj-fix lint test          # combined step keywords
/clj-fix --report           # all four steps; format reads via cljfmt check
/clj-fix format --report    # format step; no writes
/clj-fix all                # synonym for (no argument)
```

The skill writes when the `format` step runs without `--report`. With `--report`, the format step runs `cljfmt check`, which reports diffs without writing. The other three steps (`lint`, `test`, `dry`) are pure-read regardless. The `## Mutation` section in the skill body documents this asymmetry.

### `/clj-new` -- mutating skill, exemption from `all`/path rows

```
/clj-new my-service          # creates project tree at ./my-service
/clj-new                     # prompts the operator for a project name
```

`<project-name>` is a required positional argument. The skill omits the `all` and `<path>` rows because they would not be meaningful for scaffolding. It also omits `--report` because preview is meaningless: the operator reads `SKILL.md` to see what files will be written. The exemption is listed below.

### `/clj-smells-fix` -- mutating skill with `--report`

```
/clj-smells-fix                         # fix changed files; apply Stage 1 mechanical and Stage 2 DEFECT-band findings, report the rest
/clj-smells-fix src/api                 # fix files under directory
/clj-smells-fix src/api/auth.clj        # fix specific file
/clj-smells-fix all                     # fix the full codebase
/clj-smells-fix --report                # produce the report only; no writes
/clj-smells-fix src/api --report        # report-only on the given path
```

The skill writes when a finding falls within the documented safety band: Stage 1 mechanical findings from the `clj-kondo` overlay, and Stage 2 `DEFECT`-tier findings with a local well-defined rewrite. `SMELL`, `HINT`, direct `clojure.lang.RT` usage, and `DEFECT` findings outside the band remain report-only. With `--report`, everything is reported as a suggestion. The `## Mutation` section in the skill body lists the exact rewrites the band covers.

## Exemptions

`/clj-new` accepts a required positional `<project-name>` because no useful default exists for scaffolding. It omits the `all` and `<path>` scope rows and the `--report` flag for the same reason. The `## Arguments` table in its `SKILL.md` documents the positional grammar.

## Ambiguity notes

**`(no argument)` outside a git worktree.** A skill whose narrowest useful default depends on git state (for example, "review changed files") must define the fallback when no git worktree is present. The expected fallback is to ask the operator what to review rather than to widen silently to `all`.

**`commit` versus staged-and-unstaged state.** When a skill accepts `commit` as a scope keyword in a future iteration, it must state whether `commit` means the most recent commit, the staged tree, or the staged-plus-unstaged working tree. The expected default is the most recent commit. Staged-or-unstaged scopes are the narrowest default for "review my pending work," which is what `(no argument)` already means.

**`--report` versus natural-language synonyms.** "Preview," "dry run," "rehearse," and similar phrases are scope input or step keywords (or operator chatter), never mode triggers. Only the literal `--report` token disables writes. A skill that accepts both is wrong; the natural-language synonym must mean something else or be rejected.

**Step keywords versus scope keywords.** A skill like `/clj-fix` accepts step keywords (`lint`, `format`, `test`, `dry`) that select work to run, and scope keywords (`all`) that widen scope. Step keywords are skill-specific and listed in the skill's own table. Scope keywords are shared and listed here. When a skill has both, the `## Arguments` table lists both with clearly distinct rows.

## Author checklist

When adding or modifying a user-invocable skill, confirm each item before committing.

- [ ] Skill has a `## Arguments` section (or is listed under [Exemptions](#exemptions)).
- [ ] Scope rows match the canonical table; opt-in rows appear only where the skill genuinely supports them.
- [ ] If the skill mutates, the command suffix signals it (`-fix`, `-sync`, `-prune`, `-rebuild`, `-new`, `-deploy`, `-upgrade`, `-test`, `-create`, `-apply`, or the bare verbs `/commit` and `/pause`).
- [ ] If the skill mutates and preview is useful, `--report` is documented.
- [ ] If the skill is pure-report, the suffix signals it (`-review`, `-audit`, `-check`) and the skill has no `--report` flag.
- [ ] No `## Customization` section.
- [ ] No `--name` flags other than `--report`. Tool-level flags the skill calls internally (for example, `cljfmt --check`) are not skill flags and do not count.
- [ ] Mutating skills that walk the workspace include a one-line Rule 4 reference in their `## Mutation` or `## Scope` section. Exempt skills (scaffolding, dependency upgrade, fixed targets, git operations, named paths) carry no reference.
- [ ] Frontmatter `name` matches the skill's directory name.
- [ ] OpenCode mirror under `.opencode/skills/<name>/SKILL.md` is byte-identical to the canonical source.
- [ ] `agents/openai.yaml` `default_prompt` references the current command name.
