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

ClojureDart's REPL has no `in-ns`. Switch there by evaluating an `(ns my-app.core)` form, as the `clojuredart` skill describes.

## comment forms

Use `(comment ...)` forms for REPL-driven development. `comment` ignores its body and returns `nil`, so the forms inside never run when the namespace loads, but they serve as a scratchpad for interactive evaluation:

```clojure
(comment
  (process-data sample-input)
  (time (expensive-operation)))
```

Keep the closing parenthesis on the last form, as the Formatting rules require. cljfmt's default `:remove-surrounding-whitespace?` setting moves a parenthesis placed on its own line back onto the last form.
