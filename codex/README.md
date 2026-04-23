# Clojure Skills for Codex

Install the `clojure` and `clojuredart` packages in Codex via APM.

The examples below use `clojure` as the package name. Substitute `clojuredart` to install the other package, or run both commands to install both.

> **Global installs:** APM 0.8.11 warns that Codex has no native user-scope deployment. The `-g` flag is shown below for completeness but is not reliable today. Prefer per-project installs.

APM deploys skills into `.agents/skills/`.

## Install

Per-project:

```bash
apm install --target codex brackendev/clojure-skills/codex/clojure
```

Global: add `-g`.

## Update

```bash
apm deps update --target codex                                              # every project install
apm deps update --target codex brackendev/clojure-skills/codex/clojure      # one package
apm deps update -g --target codex brackendev/clojure-skills/codex/clojure   # one global install
```

## Uninstall

Add `-g` for global:

```bash
apm uninstall brackendev/clojure-skills/codex/clojure
```

## Use

Codex skills are model-invoked. Phrase requests in natural language:

```text
Use clj-new to scaffold a Clojure project called my-app.
Use clj-check to run the quality pipeline.
Use cljd-new to scaffold a ClojureDart Flutter project.
Use cljd-check to run the quality pipeline.
Use cljd-test to run the test suite.
```
