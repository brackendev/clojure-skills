# Clojure Agent Skills

[Agent Skills](https://agentskills.io) for the [Clojure](https://clojure.org) ecosystem by [brackendev](https://github.com/brackendev).

## Plugins

| Plugin | Description |
|--------|-------------|
| [**clojuredart**](README-clojuredart.md) | ClojureDart development skills (syntax, Dart interop, scaffolding, quality checks) |

## Installation

### Claude Code

#### 1. Add the Marketplace

```bash
/plugin marketplace add brackendev/clojure-agent-skills
```

#### 2. Install Plugins

```bash
# ClojureDart development
/plugin install clojuredart@clojure-agent-skills
```

#### Uninstall

```bash
/plugin uninstall clojuredart@clojure-agent-skills
/plugin marketplace remove brackendev/clojure-agent-skills
```

### Other Agent Skills-compatible tools

These skills follow the [Agent Skills](https://agentskills.io) open standard. Compatible tools include [OpenCode](https://opencode.ai), [Cursor](https://www.cursor.com), [Gemini CLI](https://github.com/google-gemini/gemini-cli), and others that support the `.claude/skills/` path.

Clone the repository and symlink skill directories from `clojuredart/skills/` into the skills directory for your tool.

## License

MIT
