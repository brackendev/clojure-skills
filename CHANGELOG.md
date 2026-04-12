# Changelog

## [Unreleased]

### clojuredart 0.1.3

#### Added

- Added `effort` frontmatter to all applicable skills. Complex skills (clojuredart-lenses, cljd-upgrade, cljd-test) use `effort: max` for deep reasoning. Routine skills (cljd-check, cljd-new, cljd-nav) use `effort: auto` for adaptive thinking. The clojuredart model-invoked style guide has no effort set and runs inline.

### clojure 0.1.3

#### Added

- Added `effort` frontmatter to all applicable skills. Complex skills (clojure-lenses) use `effort: max` for deep reasoning. Routine skills (clj-check, clj-new) use `effort: auto` for adaptive thinking. The clojure model-invoked style guide has no effort set and runs inline.

### clojuredart 0.1.2

#### Changed

- **Packaging:** Added an APM-detectable root `plugin.json` to the Codex package so `apm install --target codex brackendev/clojure-skills/codex/clojuredart` works for per-project installs.
- **clojuredart** (model-invoked): Moved Key Rules to the top of the skill for front-loaded context. Moved project workflows (MCP integration, CLI, project structure, deps.edn, tooling) to `references/project-workflows.md` to reduce always-loaded context size. Added pointer to references file after Key Rules. Removed redundant "When to Apply" section (covered by description frontmatter). Added directive decision guide for choosing between atoms, cells, `:bind`/`:get`, `:managed`, `:watch`, and `:bg-watcher`. Fixed Clojure version in deps.edn reference (1.11.0 to 1.12.0).
- **cljd-nav** (model-invoked): Removed redundant "When to Apply" section (covered by description frontmatter).
- **cljd-test** (user-invoked): Added mode-to-command table mapping `unit`, `widget`, and `all` arguments to specific `dart test` tag commands. Removed one-off `allowed-tools` frontmatter for consistency with other skills.

### clojure 0.1.2

#### Changed

- **Packaging:** Added an APM-detectable root `plugin.json` to the Codex package so `apm install --target codex brackendev/clojure-skills/codex/clojure` works for per-project installs.
- **clojure** (model-invoked): Expanded style guide coverage from bbatsov/clojure-style-guide. Moved Key Rules to the top of the skill for front-loaded context. Moved project workflows (CLI, MCP integration, project structure, deps.edn, tooling, REPL) to `references/project-workflows.md` to reduce always-loaded context size. Added laziness and realization guidance (run!, doseq, mapv, doall). Added nil-safe threading (some->, some->>, if-some, when-some). Added dispatch choice guidance (case/cond versus multimethods versus protocols). Added namespaced keys and boundary parsing with spec/Malli. Added comment form REPL workflows. Added naming conventions for side-effecting functions (`!`), CapitalCase for protocols/records/types, constants, and idiomatic parameter names. Added function design guidance for higher-order functions over loop/recur, positional parameter limits, pre/post conditions, flexible comparisons, function literals, and anonymous functions over comp/partial. Added collection idioms for vec over into, list* over cons, destructuring over index access, and record constructors. Corrected commas guidance to allow optional commas in maps. Added state management for refs, agents (send versus send-off), and io! macro. Added macro best practices for thin sugar over functions. Added comment conventions and #_ reader macro. Added namespace guidance for sorting requires, idiomatic aliases, and single-segment avoidance. Added new sections for privacy and metadata, exception handling, testing conventions, and docstrings. Softened absolute language for function length, pre/post conditions, and formatting rules.

### clojuredart 0.1.1

#### Added

- **cljd-test** (user-invoked): Scaffold and run ClojureDart tests with cljd.test. Covers unit tests, widget tests with flutter_test runner, integration tests, tag-based filtering, and deps.edn test configuration.
- **cljd-nav** (model-invoked): Navigation patterns for ClojureDart Flutter applications. Covers named routes, go_router integration with nested navigation, Navigator API for dialogs and modals, and tab navigation with BottomNavigationBar and TabBar.
- **clojuredart-lenses** (model-invoked): Translate code-lenses design philosophies (grug, APOSD, Tidy First, Parse Don't Validate, Honest Code, Legacy Code) to idiomatic ClojureDart and Flutter patterns.

#### Changed

- **clojuredart** (model-invoked): Expanded async coverage with Streams, isolates, and async function syntax. Added cells dependency chains, scoped watches for rebuild optimization, and f/widget vs f/build decision guide. Added Dart package integration workflow, platform channels and FFI reference, REPL interactive inspection with pick!/mount!, and Gotchas section covering dynamic warnings, Flutter Web issues, and common mistakes.

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
