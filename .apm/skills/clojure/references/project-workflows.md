# REPL Conventions

Host-neutral REPL practices that apply on every Clojure dialect. Host-specific tooling (Clojure CLI, `deps.edn`, `tools.build`, `clj-kondo`, `cljfmt`, Cognitect `test-runner`, nREPL, `clojure-mcp` on the JVM; the ClojureDart socket REPL; shadow-cljs and figwheel for ClojureScript) lives in the corresponding host skill.

## Cross-namespace references

Keep function and namespace references fully qualified when crossing namespace boundaries during REPL exploration:

```clojure
;; good: fully qualified across namespaces
(my-app.db/find-user id)

;; bad: unqualified, relies on use/refer
(find-user id)
```

## Switching namespaces

```clojure
(in-ns 'my-app.core)
```

## comment forms

Use `(comment ...)` forms for REPL-driven development. The compiler ignores them, but they serve as a scratchpad for interactive evaluation:

```clojure
(comment
  (process-data sample-input)
  (time (expensive-operation))
  )
```

Place the closing parenthesis on its own line so the last form is easy to evaluate independently.
