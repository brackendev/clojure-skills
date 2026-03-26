# Clojure Marketplace

[Clojure](https://clojure.org) ecosystem skills for [Claude Code](https://docs.anthropic.com/en/docs/claude-code), built on the [Agent Skills](https://agentskills.io) open standard.

## Skills

| Package | Description |
|---------|-------------|
| **clojure** | Clojure development skills (style guide, scaffolding, quality checks) |
| **clojuredart** | ClojureDart development skills (syntax, Dart interop, scaffolding, quality checks) |

## Installation

### Claude Code

#### 1. Add the Marketplace

```bash
/plugin marketplace add brackendev/clojure-marketplace
```

#### 2. Install Plugins

```bash
# Clojure development
/plugin install clojure@clojure-marketplace

# ClojureDart development
/plugin install clojuredart@clojure-marketplace
```

#### Uninstall

```bash
/plugin uninstall clojure@clojure-marketplace
/plugin uninstall clojuredart@clojure-marketplace
/plugin marketplace remove brackendev/clojure-marketplace
```

### Other Agent Skills-compatible tools

These skills follow the [Agent Skills](https://agentskills.io) open standard. Compatible tools include [OpenCode](https://opencode.ai), [Cursor](https://www.cursor.com), [Gemini CLI](https://github.com/google-gemini/gemini-cli), and others that support the `.claude/skills/` path.

Clone the repository and symlink skill directories from `clojure/skills/` or `clojuredart/skills/` into the skills directory for your tool.

**OpenCode** searches `~/.config/opencode/skills/<name>/SKILL.md` and `~/.claude/skills/<name>/SKILL.md`. Create symlinks from the marketplace skills:

```bash
mkdir -p ~/.config/opencode/skills
cd ~/.claude/plugins/marketplaces/brackendev/
ln -s clojure-marketplace/clojure/skills/* ~/.config/opencode/skills/
ln -s clojure-marketplace/clojuredart/skills/* ~/.config/opencode/skills/
```

## clojure

Clojure development skills: idiomatic style guide, project scaffolding, and quality checks.

### `/clojure:clj-new <project-name>`

**Does:** Scaffold a new Clojure project with deps.edn, source layout, test runner, tools.build, and cljfmt configured.

**Produces:** Project directory with standard Clojure layout

```bash
/clojure:clj-new my-app
```

### `/clojure:clj-check [lint|format|test]`

**Does:** Run the Clojure quality pipeline. Lints with clj-kondo, formats with cljfmt, and runs tests with cognitect test-runner. Run all steps or specify individual steps.

**Produces:** Pass/fail, fixed code

```bash
/clojure:clj-check              # run all steps
/clojure:clj-check lint         # lint only
/clojure:clj-check format       # format only
/clojure:clj-check test         # test only
```

### Auto-Triggered

| Skill | Triggers |
|-------|----------|
| **clojure** | Working with `.clj` files or `deps.edn` projects. Provides curated style guide rules from [bbatsov/clojure-style-guide](https://github.com/bbatsov/clojure-style-guide), covering naming conventions, idiomatic patterns, threading macros, variable binding, control flow, collection idioms, and common anti-patterns to avoid. |

## clojuredart

[ClojureDart](https://github.com/Tensegritics/ClojureDart) development skills: syntax knowledge, Dart interop patterns, project scaffolding, and quality checks.

### `/clojuredart:cljd-new <project-name>`

**Does:** Scaffold a new ClojureDart Flutter project. Creates the Flutter project, configures `deps.edn` with ClojureDart, sets up the entry point, formatting rules, and runs an initial compile.

**Produces:** Flutter project directory with ClojureDart configured

```bash
/clojuredart:cljd-new my-app
```

### `/clojuredart:cljd-check [lint|format|compile]`

**Does:** Run the ClojureDart quality pipeline. Lints with clj-kondo, formats with cljfmt, and compiles with the ClojureDart compiler. Run all steps or specify individual steps.

**Produces:** Pass/fail, fixed code

```bash
/clojuredart:cljd-check              # run all steps
/clojuredart:cljd-check lint         # lint only
/clojuredart:cljd-check format       # format only
/clojuredart:cljd-check compile      # compile only
```

### `/clojuredart:cljd-upgrade`

**Does:** Upgrade the ClojureDart dependency to the latest version.

**Produces:** Updated deps.edn

```bash
/clojuredart:cljd-upgrade
```

### Auto-Triggered

| Skill | Triggers |
|-------|----------|
| **clojuredart** | Working with `.cljd` files, `deps.edn` containing ClojureDart, or Flutter projects with `cljd-out/` directories. Provides ClojureDart syntax, Dart interop patterns, namespace conventions, project structure, and compilation knowledge. |

## Prerequisites

### clojure

- [Clojure CLI](https://clojure.org/guides/install_clojure) (tools.deps)
- [clj-kondo](https://github.com/clj-kondo/clj-kondo)

### clojuredart

- [Clojure CLI](https://clojure.org/guides/install_clojure) (tools.deps)
- [Flutter SDK](https://docs.flutter.dev/get-started/install)
- [clj-kondo](https://github.com/clj-kondo/clj-kondo)
- [ClojureDart](https://github.com/Tensegritics/ClojureDart)

## Recommended

- [bhauman/clojure-mcp](https://github.com/bhauman/clojure-mcp) -- MCP server for REPL-driven Clojure development. Provides REPL evaluation, namespace management, and file operations.
