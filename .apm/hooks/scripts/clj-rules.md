# Clojure rules from clojure-skills

This is a Clojure-family project. The Iron Laws below come from the `clojure` skill. Never write code that breaks one. Before writing, reviewing, or refactoring Clojure code, load the `clojure` skill and the host skill for this project (`clojure-jvm`, `clojurescript`, or `clojuredart`, and a framework skill such as `biff`, `electric`, or `fulcro` when it applies). The skills hold the full laws, their exceptions, and the references.

## Iron Laws

- CLJ-1: Never `def` inside a function body. Bind locals with `let`, and hold changing state in an atom.
- CLJ-2: Realize lazy sequences before their scope closes. Use `doall`, `vec`, or `mapv` inside `with-open`, `binding`, or `try`, and use `doseq` or `run!` for side effects, never `map` or `for`.
- CLJ-3: Keep `swap!` functions free of side effects, because they may run more than once. Act after `swap!` returns.
- CLJ-4: Use the return value of every transient operation. Never call `assoc!` or `conj!` for its effect in place.
- CLJ-5: `contains?` checks keys, never values. On a vector it tests an index. Test membership with a set or `some`.
- CLJ-6: `case` constants are literals, never vars. Compare against runtime values with `condp =` or `cond`.
- CLJ-7: Evaluate each macro argument at most once. Bind it with an auto-gensym, unless repeating it is the macro's purpose, as in a loop body.
- CLJ-8: No side effects when a namespace loads. Start connections, threads, and I/O from an explicit function, a `delay`, or a component system.
- CLJ-9: Never use floating point for money. Use an exact decimal type where the host has one, and integer minor units such as cents where it does not.
- CLJ-10: Read untrusted text only with an EDN reader, never `read-string`, `read`, `eval`, or `load-string`, and register only safe tag readers.
- CLJ-11: Never splice input into SQL or shell commands. Pass SQL values as parameters, map identifiers through an allowlist, and pass a program and its arguments as separate strings.
- CLJ-12: Verify from saved files, not REPL state. Run the tests or lint in a fresh process, or after a full reload, before reporting success.

## Design

Let catalog data own policy (Key Rule 19). When a catalog, registry, or configuration map describes the things the program works with, behavior and the relationships between entries come from those declarations. Code never names a specific entry to decide what to do. A relationship that no entry declares means no behavior, never a hardcoded fallback (Honest Code P15 and P16). One pure function answers each question about the data, and every caller asks it. Validate the data when it loads. Closed-case dispatch on a fixed set of variants stays fine. The `clojure` skill's `references/data-driven-design.md` has examples.
