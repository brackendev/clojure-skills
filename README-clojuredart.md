# clojuredart

[ClojureDart](https://github.com/Tensegritics/ClojureDart) development skills: syntax knowledge, Dart interop patterns, project scaffolding, and quality checks.

## Skills

All skills follow the [Agent Skills](https://agentskills.io) open standard.

### `clojuredart` (auto-triggered)

Activates when working with `.cljd` files, `deps.edn` containing ClojureDart, or Flutter projects with `cljd-out/` directories. Provides ClojureDart syntax, Dart interop patterns, namespace conventions, project structure, and compilation knowledge.

### `/clojuredart:cljd-new <project-name>`

Scaffolds a new ClojureDart Flutter project. Creates the Flutter project, configures `deps.edn` with ClojureDart, sets up the entry point, formatting rules, and runs an initial compile.

### `/clojuredart:cljd-check [lint|format|compile]`

Runs the ClojureDart quality pipeline. Lints with clj-kondo, formats with cljfmt, and compiles with the ClojureDart compiler. Run all steps or specify individual steps.

## Prerequisites

- [Clojure CLI](https://clojure.org/guides/install_clojure) (tools.deps)
- [Flutter SDK](https://docs.flutter.dev/get-started/install)
- [clj-kondo](https://github.com/clj-kondo/clj-kondo)
- [ClojureDart](https://github.com/Tensegritics/ClojureDart)
