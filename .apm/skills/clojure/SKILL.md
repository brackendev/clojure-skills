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

- [clojure-jvm-skills](https://github.com/brackendev/clojure-jvm-skills) -- JVM Clojure: Java interop, refs / agents / STM, `with-open` over Java resources, JVM-typed exceptions, `BigDecimal` and the other JVM number types, `alter-var-root`, Clojure CLI / `tools.build` / `clj-kondo` / `cljfmt` / test-runner / nREPL workflow.
- [clojurescript-skills](https://github.com/brackendev/clojurescript-skills) -- ClojureScript on JavaScript hosts: JS interop, externs and advanced compilation, macro stage separation, `catch :default`, cleanup with `try`/`finally` (ClojureScript has no `with-open`), JS-flavored numerics and truthiness, the `cljs.main` workflow.
- [clojuredart-skills](https://github.com/brackendev/clojuredart-skills) -- ClojureDart on Flutter: Dart interop, type hints and nullability, `cljd.flutter` directives, Dart error types and `with-open`, which calls `.dispose`, async, FFI, REPL.
- [biff-skills](https://github.com/brackendev/biff-skills) -- the [Biff](https://biffweb.com/) 2 web framework on the JVM.
- [electric-skills](https://github.com/brackendev/electric-skills) -- the [Electric](https://github.com/hyperfiddle/electric) v3 full-stack reactive framework from Hyperfiddle.
- [fulcro-skills](https://github.com/brackendev/fulcro-skills) -- the [Fulcro](https://github.com/fulcrologic/fulcro) 3 full-stack framework and its Pathom 3 server.

This skill does not duplicate that material. When in JVM, ClojureScript, or ClojureDart context, defer to the host skill for its named domains and keep using this skill for everything else.

## Iron Laws

These rules prevent bugs, data loss, security holes, and false reports of success. Never write code that breaks one. When existing code breaks a law, stop and report the law ID, the location, the offending code, and the correct pattern, then ask before changing code outside the current task. A law overrides any Key Rule or reference that seems to conflict with it. Host and framework skills add their own laws with the prefixes `JVM`, `CLJS`, `CLJD`, `BIFF`, `ELEC`, and `FUL`.

1. **CLJ-1: NEVER `def` INSIDE A FUNCTION BODY.** `def` interns a namespace-global var when the function compiles and resets its value on every call, so concurrent calls race and values leak from one call into the next. Bind locals with `let`, and hold changing state in an atom (`references/state-and-dispatch.md`).
2. **CLJ-2: REALIZE LAZY SEQUENCES BEFORE THEIR SCOPE CLOSES.** A lazy sequence returned from the body of `with-open`, `binding`, or `try` is realized after the resource is closed, the binding has ended, or the handler has exited. On every host, `(binding [*rate* 10] (map #(* % *rate*) xs))` computes its values with the root `*rate*`, and an exception thrown while a lazy result of `try` is read escapes the `catch`. Realize the result inside the scope with `doall`, `vec`, or `mapv`. `clojure-jvm/references/errors-and-resources.md` in the `clojure-jvm` skill (clojure-jvm-skills) shows the JVM reader that fails with "Stream closed", and `clojuredart/references/errors-and-resources.md` in the `clojuredart` skill (clojuredart-skills) covers ClojureDart's `with-open`. Never use `map` or `for` only for side effects, because their effects run only when someone consumes the result. Use `doseq` or `run!` (`references/collections.md`).
3. **CLJ-3: KEEP `swap!` FUNCTIONS FREE OF SIDE EFFECTS.** The `swap!` docstring says the function "may be called multiple times." JVM Clojure retries it under contention. ClojureScript and ClojureDart call it once, but portable code must not depend on that. Compute the new value in the function, and act after `swap!` returns (`references/state-and-dispatch.md`).
4. **CLJ-4: USE THE RETURN VALUE OF EVERY TRANSIENT OPERATION.** clojure.org: transients "are not designed to be bashed in-place. You must capture and use the return value in the next call." On JVM Clojure and ClojureScript, calling `assoc!` twenty times in place on one transient map keeps only 8 entries (`references/collections.md`).
5. **CLJ-5: `contains?` CHECKS KEYS, NEVER VALUES.** On a vector it tests whether an index exists: `(contains? [:a :b] :a)` is `false`. Test membership with a set, as in `(#{:a :b} x)`, or with `some` (`references/collections.md`).
6. **CLJ-6: `case` CONSTANTS ARE LITERALS, NEVER VARS.** `case` does not evaluate its test constants, so `(case x limit ...)` compares `x` with the symbol `limit`, not with its value. Use `condp =` or `cond` to compare against runtime values (`references/naming-and-functions.md`).
7. **CLJ-7: EVALUATE EACH MACRO ARGUMENT AT MOST ONCE.** Every place a macro unquotes an argument becomes a copy of that expression, so an argument unquoted twice runs its side effects twice. Bind it once with an auto-gensym, as in `` `(let [v# ~x] ...) ``, unless repeating it is the macro's purpose, as in a loop body (`references/macros.md`).
8. **CLJ-8: NO SIDE EFFECTS WHEN A NAMESPACE LOADS.** A top-level form that opens a connection, starts a thread, or performs I/O runs whenever the namespace loads: in tests, at the REPL, on reload, and on the JVM during AOT compilation. Start resources from an explicit function, a `delay`, or a component system (`references/namespaces.md`).
9. **CLJ-9: NEVER USE FLOATING POINT FOR MONEY.** `(+ 0.1 0.2)` is `0.30000000000000004` on every host. Keep money in an exact decimal type from parsing to storage where the host has one, and never mix it with floating-point values. Where the host has none, use integer minor units such as cents (`references/collections.md`). The JVM has `BigDecimal`, which `clojure-jvm/references/numbers.md` in the `clojure-jvm` skill (clojure-jvm-skills) covers. ClojureScript and ClojureDart have no decimal type and read `0.1M` as an ordinary double, so they use integer minor units. `clojurescript/references/host-semantics.md` in the `clojurescript` skill (clojurescript-skills) covers JavaScript numbers, and `clojuredart/references/errors-and-resources.md` in the `clojuredart` skill (clojuredart-skills) covers ClojureDart's `M` literals.
10. **CLJ-10: READ UNTRUSTED TEXT ONLY WITH AN EDN READER.** `clojure.core/read-string` and `read` "can execute code" and "should be used only with trusted sources," and `eval` and `load-string` run whatever they receive. Use `clojure.edn/read-string` on the JVM, `cljs.reader/read-string` in ClojureScript, and `cljd.edn/read-string` in ClojureDart, and register only safe tag readers (`references/untrusted-input.md`).
11. **CLJ-11: NEVER SPLICE INPUT INTO SQL OR SHELL COMMANDS.** Pass SQL values as parameters, as in `["select * from users where id = ?" id]`: a query string with `?` placeholders followed by the values, the shape that next.jdbc accepts and HoneySQL's `format` returns. Map identifiers such as column names or sort directions through an allowlist. Pass the program and each argument as separate strings, never as one command string that a shell interprets, and still validate arguments that reach another program (`references/untrusted-input.md`).
12. **CLJ-12: VERIFY FROM SAVED FILES, NOT REPL STATE.** A form that works in the REPL proves nothing about the files, and reloading a namespace does not remove stale definitions. Write the change to the source file, then run the project's tests or lint in a fresh process, or after a full reload, before reporting success. The `clojure-repl` skill covers stale REPL state.

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
7. **No commas in sequential collections.** Commas are optional in maps.
8. **Prefer vectors over quoted lists.** `[1 2 3]` not `'(1 2 3)`.
9. **Do not write macros when functions work.** Macros complicate debugging and composition.
10. **Keep functions short.** Aim for under 10 LOC. Most work well under 5 LOC.
11. **Name side-effecting functions with `!`.** `save-user!` not `save-user`.
12. **Prefer higher-order functions over `loop/recur`.** Use `map`, `filter`, `reduce`.
13. **Limit positional parameters to three or four.** Use an options map beyond that.
14. **Use `defn-` for private functions.** Use `^:private` for private vars.
15. **Use `->Foo` constructors for records.** Not interop syntax. Applies on every host that ships `defrecord`. ClojureDart adds a further reason: the raw interop form requires extra meta/extmap/hash arguments the factory supplies automatically.
16. **Use `some->` and `some->>` for nil-safe pipelines.** Short-circuits on first `nil`.
17. **Throw `ex-info` with a data map, and keep the cause when rethrowing.** Pass the caught exception as the third argument so the root cause stays in the chain. Host-typed exceptions belong in the host skill.
18. **Use the core map, parsing, and UUID functions over hand-rolled equivalents.** `update-vals`, `update-keys`, `parse-long`, `parse-double`, `parse-boolean`, `parse-uuid`, `random-uuid`, and `abs` exist on every host. See "Core functions for maps, parsing, and UUIDs" in `references/collections.md`.
19. **Let catalog data own policy.** When a catalog, registry, or configuration map describes the things a program works with, behavior and the relationships between entries come from those declarations. Code reads the declarations and never names a specific entry to decide what to do. A relationship that no entry declares means no behavior, never a hardcoded fallback. One pure function answers each question about the data, and every caller asks that function instead of deriving its own answer. Validate the data when it loads. Closed-case dispatch on a fixed set of variants stays fine. See `references/data-driven-design.md`.

## References

The Iron Laws and Key Rules are the short form. Each reference file below holds the detailed guidance and the good and bad examples for one topic. Before writing, reviewing, or refactoring Clojure code, read every reference whose topic the task touches, and read the next one when the work moves to a new topic.

| When the task involves | Read |
|------------------------|------|
| Naming functions, vars, parameters, or predicates, or designing a function: its length, arity, parameters, conditionals (`when`, `if-let`, `condp`, `case` and its literal constants), function literals, or pre and post conditions | `references/naming-and-functions.md` |
| Threading macros, `let` bindings and destructuring, `nil`-returning lookups, or throwing and rethrowing `ex-info` | `references/control-flow.md` |
| Collections, keyword and set idioms, `contains?`, transients, empty checks, laziness, strings, numbers and money, or the core map, parsing, and UUID functions (`update-vals`, `parse-long`, `random-uuid`) | `references/collections.md` |
| Atoms and other state, multimethods, protocols, or choosing a dispatch mechanism | `references/state-and-dispatch.md` |
| Catalogs, registries, or configuration data that decide behavior: declaring relationships between entries, resolving them with pure functions, validating the data when it loads, or code that branches on specific entry names | `references/data-driven-design.md` |
| Writing or reviewing a macro | `references/macros.md` |
| `ns` forms, requires and aliases, private vars, or metadata | `references/namespaces.md` |
| Parsing data from outside the program, or building SQL queries and shell commands | `references/untrusted-input.md` |
| Portable `.cljc` code: reader conditionals (`#?`, `#?@`), platform tags, conditional requires, or a catch-all `catch` type for several hosts | `references/reader-conditionals.md` |
| Formatting, indentation, comments, or docstrings | `references/formatting.md` |
| Writing or reviewing tests | `references/testing.md` |
| REPL conventions: qualified cross-namespace calls, `in-ns`, and `comment` forms | `references/project-workflows.md` |

Three task skills in this package cover workflows rather than style. Load `clojure-repl` to evaluate code in a running REPL, `clojure-debug` when code, a build, or a test fails with an error, and `clojure-deps` to check a dependency's version, releases, or documentation.
