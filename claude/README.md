# Clojure Skills for Claude Code

Install, update, and use the `clojure` and `clojuredart` packages in Claude Code.

## Install

### With APM

`clojure` per-project:

```bash
apm install --target claude brackendev/clojure-skills/claude/clojure
```

`clojure` global:

```bash
apm install -g --target claude brackendev/clojure-skills/claude/clojure
```

`clojuredart` per-project:

```bash
apm install --target claude brackendev/clojure-skills/claude/clojuredart
```

`clojuredart` global:

```bash
apm install -g --target claude brackendev/clojure-skills/claude/clojuredart
```

APM deploys these skills into `.claude/skills/`.

Remove them with:

```bash
apm uninstall --target claude brackendev/clojure-skills/claude/clojure
apm uninstall --target claude brackendev/clojure-skills/claude/clojuredart
```

Remove global installs with:

```bash
apm uninstall -g --target claude brackendev/clojure-skills/claude/clojure
apm uninstall -g --target claude brackendev/clojure-skills/claude/clojuredart
```

### With Claude Marketplace

```bash
claude plugins marketplace add brackendev/clojure-skills
claude plugins install clojure@clojure-skills
claude plugins install clojuredart@clojure-skills
```

Remove them with:

```bash
claude plugins uninstall clojure
claude plugins uninstall clojuredart
```

## Update

### APM Installs

Update all project-scoped installs from the project root:

```bash
apm deps update --target claude
```

Update one package:

```bash
apm deps update --target claude brackendev/clojure-skills/claude/clojure
apm deps update --target claude brackendev/clojure-skills/claude/clojuredart
```

Update global installs:

```bash
apm deps update -g --target claude brackendev/clojure-skills/claude/clojure
apm deps update -g --target claude brackendev/clojure-skills/claude/clojuredart
```

### Claude Marketplace

Refresh marketplace metadata:

```bash
claude plugins marketplace update clojure-skills
```

Update installed plugins:

```bash
claude plugins update clojure
claude plugins update clojuredart
```

If the plugin was installed outside the default user scope, pass the matching scope to the update command, for example `claude plugins update -s project clojure`.

## Use

```text
/clojure:clj-check
/clojure:clj-new my-app
/clojuredart:cljd-check
/clojuredart:cljd-test
```
