# Clojure Skills for Claude Code

Install the `clojure` and `clojuredart` packages in Claude Code via APM or the native Claude marketplace.

The examples below use `clojure` as the package name. Substitute `clojuredart` to install the other package, or run both commands to install both.

## APM

APM deploys skills into `.claude/skills/`.

Install (per-project):

```bash
apm install --target claude brackendev/clojure-skills/claude/clojure
```

Install (global): add `-g`.

Update:

```bash
apm deps update --target claude                                              # every project install
apm deps update --target claude brackendev/clojure-skills/claude/clojure     # one package
apm deps update -g --target claude brackendev/clojure-skills/claude/clojure  # one global install
```

Uninstall (add `-g` for global):

```bash
apm uninstall brackendev/clojure-skills/claude/clojure
```

## Claude Marketplace

Add the marketplace once:

```bash
claude plugins marketplace add brackendev/clojure-skills
```

Install:

```bash
claude plugins install clojure@clojure-skills
```

Update:

```bash
claude plugins marketplace update clojure-skills   # refresh marketplace metadata
claude plugins update clojure
```

If a plugin was installed outside the default user scope, pass `-s <scope>` to the update command (for example `-s project`).

Uninstall:

```bash
claude plugins uninstall clojure
```

## Use

```text
/clojure:clj-check
/clojure:clj-new my-app
/clojuredart:cljd-check
/clojuredart:cljd-test
```
