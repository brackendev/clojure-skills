# Clojure Agent Skills

[Agent Skills](https://agentskills.io) for the [Clojure](https://clojure.org) ecosystem by [brackendev](https://github.com/brackendev).

## Plugins

| Plugin | Description |
|--------|-------------|
| **clojure** | Clojure development skills (style guide, scaffolding, quality checks) |
| **clojuredart** | ClojureDart development skills (syntax, Dart interop, scaffolding, quality checks) |

## Installation

### Claude Code

#### 1. Add the Marketplace

```bash
/plugin marketplace add brackendev/clojure-agent-skills
```

#### 2. Install Plugins

```bash
# Clojure development
/plugin install clojure@clojure-agent-skills

# ClojureDart development
/plugin install clojuredart@clojure-agent-skills
```

#### Uninstall

```bash
/plugin uninstall clojure@clojure-agent-skills
/plugin uninstall clojuredart@clojure-agent-skills
/plugin marketplace remove brackendev/clojure-agent-skills
```

### Other Agent Skills-compatible tools

These skills follow the [Agent Skills](https://agentskills.io) open standard. Compatible tools include [OpenCode](https://opencode.ai), [Cursor](https://www.cursor.com), [Gemini CLI](https://github.com/google-gemini/gemini-cli), and others that support the `.claude/skills/` path.

Clone the repository and symlink skill directories from `clojure/skills/` or `clojuredart/skills/` into the skills directory for your tool.

---

## clojure

Clojure development skills: idiomatic style guide, project scaffolding, and quality checks.

### Skills

All skills follow the [Agent Skills](https://agentskills.io) open standard.

#### `clojure` (auto-triggered)

Activates when working with `.clj` files or `deps.edn` projects. Provides curated style guide rules from [bbatsov/clojure-style-guide](https://github.com/bbatsov/clojure-style-guide), covering naming conventions, idiomatic patterns, threading macros, variable binding, control flow, collection idioms, and common anti-patterns to avoid.

#### `/clojure:clj-new <project-name>`

Scaffolds a new Clojure project with deps.edn, source layout, test runner, tools.build, and cljfmt configured.

#### `/clojure:clj-check [lint|format|test]`

Runs the Clojure quality pipeline. Lints with clj-kondo, formats with cljfmt, and runs tests with cognitect test-runner. Run all steps or specify individual steps.

### Recommended

- [bhauman/clojure-mcp](https://github.com/bhauman/clojure-mcp) -- MCP server for REPL-driven Clojure development. Provides REPL evaluation, namespace management, and file operations. The clojure plugin's style guide and REPL practices complement this MCP.

### Prerequisites

- [Clojure CLI](https://clojure.org/guides/install_clojure) (tools.deps)
- [clj-kondo](https://github.com/clj-kondo/clj-kondo)

---

## clojuredart

[ClojureDart](https://github.com/Tensegritics/ClojureDart) development skills: syntax knowledge, Dart interop patterns, project scaffolding, and quality checks.

### Skills

All skills follow the [Agent Skills](https://agentskills.io) open standard.

#### `clojuredart` (auto-triggered)

Activates when working with `.cljd` files, `deps.edn` containing ClojureDart, or Flutter projects with `cljd-out/` directories. Provides ClojureDart syntax, Dart interop patterns, namespace conventions, project structure, and compilation knowledge.

#### `/clojuredart:cljd-new <project-name>`

Scaffolds a new ClojureDart Flutter project. Creates the Flutter project, configures `deps.edn` with ClojureDart, sets up the entry point, formatting rules, and runs an initial compile.

#### `/clojuredart:cljd-check [lint|format|compile]`

Runs the ClojureDart quality pipeline. Lints with clj-kondo, formats with cljfmt, and compiles with the ClojureDart compiler. Run all steps or specify individual steps.

### Prerequisites

- [Clojure CLI](https://clojure.org/guides/install_clojure) (tools.deps)
- [Flutter SDK](https://docs.flutter.dev/get-started/install)
- [clj-kondo](https://github.com/clj-kondo/clj-kondo)
- [ClojureDart](https://github.com/Tensegritics/ClojureDart)

---

## License

MIT
