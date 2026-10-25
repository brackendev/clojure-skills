---
name: clojure
description: >-
  Use when writing, editing, reviewing, or discussing Clojure family code.
  Triggers: .clj, .cljs, .cljc, and .cljd files; deps.edn projects;
  shadow-cljs.edn; bb.edn; project.clj; build.clj; clojure.test, cljs.test,
  cljd.test; clojure.core forms (`defn`, `let`, `ns`, threading macros,
  destructuring, multimethods, protocols, transducers, specs, atoms); REPL
  usage; or any mention of Clojure, ClojureScript, ClojureDart, or a Clojure
  dialect. Covers idiomatic style, naming conventions, threading macros,
  collection idioms, atom-based state, dispatch, formatting, namespaces, and
  common anti-patterns.
user-invocable: false
---

# Clojure

This skill defines host-neutral Clojure family guidance. It applies to JVM Clojure (`.clj`), ClojureScript (`.cljs`), portable code (`.cljc`), and ClojureDart (`.cljd`).

Host-specific guidance lives in companion skills, which override this baseline only in named domains: host interop, host-typed exception handling, host resource cleanup, runtime-specific concurrency primitives (refs, agents, STM), `alter-var-root` and var rebinding semantics, host-specific aliases, host-specific test runner ergonomics, host-specific build and run tooling.

- [clojure-jvm-skills](https://github.com/brackendev/clojure-jvm-skills) -- JVM Clojure: Java interop, refs / agents / STM, `with-open`, JVM-typed exceptions, `alter-var-root`, Clojure CLI / `tools.build` / `clj-kondo` / `cljfmt` / test-runner / nREPL workflow.
- [clojurescript-skills](https://github.com/brackendev/clojurescript-skills) -- ClojureScript on JavaScript hosts: JS interop, externs and advanced compilation, macro stage separation, `catch :default`, JS-flavored numerics and truthiness, the `cljs.main` workflow.
- [clojuredart-skills](https://github.com/brackendev/clojuredart-skills) -- ClojureDart on Flutter: Dart interop, type hints and nullability, `cljd.flutter` directives, async, FFI, REPL.
- [biff-skills](https://github.com/brackendev/biff-skills) -- the [Biff](https://biffweb.com/) 2 web framework on the JVM.
- [electric-skills](https://github.com/brackendev/electric-skills) -- the [Electric](https://github.com/hyperfiddle/electric) v3 full-stack reactive framework from Hyperfiddle.
- [fulcro-skills](https://github.com/brackendev/fulcro-skills) -- the [Fulcro](https://github.com/fulcrologic/fulcro) 3 full-stack framework and its Pathom 3 server.

This skill does not duplicate that material. When in JVM, ClojureScript, or ClojureDart context, defer to the host skill for its named domains and keep using this skill for everything else.

## REPL-Driven Development

REPL-driven development is the primary workflow for Clojure family code. Prefer evaluating small forms in a running REPL before changing larger code paths, and let file edits follow from working REPL code, not the other way around. When the runtime has REPL tooling (an nREPL client, a socket REPL connection, or an editor integration) and no REPL is running, start one before continuing. The host skill names the dialect-specific command (nREPL on the JVM, the socket REPL on ClojureDart). When the runtime has no REPL tooling, do not stall on starting one: edit the files and verify with the project's test and lint commands.

For host-neutral REPL conventions (qualified cross-namespace calls, `in-ns`, and `comment` forms), read `references/project-workflows.md`. The `clojure-repl` skill covers finding the project's REPL, choosing a client, and evaluating safely.

## Key Rules

1. **Prefer `clojure.string` over host string interop.** Use `str/upper-case` not a host-native method.
2. **Use keywords as map lookup functions.** `(:name m)` not `(get m :name)`.
3. **Use sets as predicates.** `(remove #{1} coll)` not `(remove #(= % 1) coll)`.
4. **Use `seq` for empty checks.** `(when (seq s) ...)` not `(when-not (empty? s) ...)`.
5. **Use `when` for single-branch side effects.** Reserve `if` for two branches.
6. **Use `condp` or `case` over repeated `=` in `cond`.** Reduces duplication.
7. **No `def` inside `defn`.** Use `let` for local bindings.
8. **No commas in sequential collections.** Commas are optional in maps.
9. **Prefer vectors over quoted lists.** `[1 2 3]` not `'(1 2 3)`.
10. **Do not write macros when functions work.** Macros complicate debugging and composition.
11. **Keep functions short.** Aim for under 10 LOC. Most work well under 5 LOC.
12. **Name side-effecting functions with `!`.** `save-user!` not `save-user`.
13. **Prefer higher-order functions over `loop/recur`.** Use `map`, `filter`, `reduce`.
14. **Limit positional parameters to three or four.** Use an options map beyond that.
15. **Use `defn-` for private functions.** Use `^:private` for private vars.
16. **Use `->Foo` constructors for records.** Not interop syntax. Applies on every host that ships `defrecord`. ClojureDart adds a further reason: the raw interop form requires extra meta/extmap/hash arguments the factory supplies automatically.
17. **Realize lazy sequences when side effects matter.** Use `run!`, `doseq`, `mapv`, or `doall`.
18. **Use `some->` and `some->>` for nil-safe pipelines.** Short-circuits on first `nil`.
19. **Prefer `ex-info` for data-carrying exceptions.** Host-typed exceptions belong in the host skill.
20. **Use the Clojure 1.11 core functions over hand-rolled equivalents.** `update-vals`, `update-keys`, `parse-long`, `parse-double`, `parse-boolean`, `parse-uuid`, `random-uuid`, and `abs` exist on every host. See "Core functions from Clojure 1.11" in `references/collections.md`.

## References

The Key Rules are the short form. Each reference file below holds the detailed guidance and the good and bad examples for one topic. Before writing, reviewing, or refactoring Clojure code, read every reference whose topic the task touches, and read the next one when the work moves to a new topic.

| When the task involves | Read |
|------------------------|------|
| Naming functions, vars, parameters, or predicates, or designing a function: its length, arity, parameters, conditionals (`when`, `if-let`, `condp`, `case`), function literals, or pre and post conditions | `references/naming-and-functions.md` |
| Threading macros, `let` bindings and destructuring, `nil`-returning lookups, or throwing `ex-info` | `references/control-flow.md` |
| Collections, keyword and set idioms, empty checks, laziness, strings, numbers, or the Clojure 1.11 core functions | `references/collections.md` |
| Atoms and other state, multimethods, protocols, or choosing a dispatch mechanism | `references/state-and-dispatch.md` |
| Writing or reviewing a macro | `references/macros.md` |
| `ns` forms, requires and aliases, private vars, or metadata | `references/namespaces.md` |
| Formatting, indentation, comments, or docstrings | `references/formatting.md` |
| Writing or reviewing tests | `references/testing.md` |
| REPL conventions: qualified cross-namespace calls, `in-ns`, and `comment` forms | `references/project-workflows.md` |

Three task skills in this package cover workflows rather than style. Load `clojure-repl` to evaluate code in a running REPL, `clojure-debug` when code, a build, or a test fails with an error, and `clojure-deps` to check a dependency's version, releases, or documentation.
