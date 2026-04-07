# Clojure Skills for Codex

Install, update, and use the `clojure` and `clojuredart` packages in Codex.

## Install

`clojure` per-project:

```bash
apm install --target codex brackendev/clojure-skills/codex/clojure
```

`clojure` global:

```bash
apm install -g --target codex brackendev/clojure-skills/codex/clojure
```

`clojuredart` per-project:

```bash
apm install --target codex brackendev/clojure-skills/codex/clojuredart
```

`clojuredart` global:

```bash
apm install -g --target codex brackendev/clojure-skills/codex/clojuredart
```

Per-project installs deploy these skills into `.agents/skills/`.

APM 0.8.11 currently warns that Codex does not have native user-scope deployment support, so the global commands above are not reliable today. Prefer per-project Codex installs.

## Update

Update all project-scoped installs from the project root:

```bash
apm deps update --target codex
```

Update one package:

```bash
apm deps update --target codex brackendev/clojure-skills/codex/clojure
apm deps update --target codex brackendev/clojure-skills/codex/clojuredart
```

Update global installs:

```bash
apm deps update -g --target codex brackendev/clojure-skills/codex/clojure
apm deps update -g --target codex brackendev/clojure-skills/codex/clojuredart
```

Codex global updates have the same current limitation as Codex global installs.

## Use

```text
Use clj-new to scaffold a Clojure project called my-app.
Use clj-check to run the quality pipeline.
Use cljd-new to scaffold a ClojureDart Flutter project.
Use cljd-check to run the quality pipeline.
Use cljd-test to run the test suite.
```
