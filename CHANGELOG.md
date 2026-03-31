# Changelog

## 0.1.0

Restructure repository for multi-platform support. Plugins are now grouped under `claude/` and `codex/` directories. All plugin versions reset to 0.1.0.

### clojure 0.1.0

- **clojure** (model-invoked): Curated Clojure style guide rules from bbatsov/clojure-style-guide, naming conventions, idiomatic patterns, threading macros, collection idioms, state management, and common anti-patterns.
- **clj-new** (user-invoked): Scaffold a new Clojure project with deps.edn, test runner, tools.build, and cljfmt.
- **clj-check** (user-invoked): Run the Clojure quality pipeline (clj-kondo lint, cljfmt format, test runner).
- **clojure-lenses** (model-invoked): Translate code-lenses design philosophies (grug, APOSD, Tidy First, Parse Don't Validate, Honest Code, Legacy Code) to idiomatic Clojure patterns.

### clojuredart 0.1.0

- **clojuredart** (model-invoked): Core ClojureDart knowledge including Dart interop syntax, type system, cljd.flutter directives, class creation, async, destructuring patterns, cells, REPL, and CLI reference.
- **cljd-new** (user-invoked): Scaffold a new ClojureDart Flutter project with deps.edn, entry point, formatting config, and initial compile.
- **cljd-check** (user-invoked): Run the ClojureDart quality pipeline (clj-kondo lint, cljfmt format, ClojureDart compile).
- **cljd-upgrade** (user-invoked): Upgrade ClojureDart dependency in deps.edn to the latest commit.
