# Macros

Part of the `clojure` skill. This host-neutral guidance applies to `.clj`, `.cljs`, `.cljc`, and `.cljd` code. The ClojureScript skill covers macro stage separation.

## Macros

Do not write a macro when a function works:

```clojure
;; good
(defn square [x]
  (* x x))

;; bad: macro for no reason
(defmacro square [x]
  `(* ~x ~x))
```

Prefer syntax-quote over manual list construction:

```clojure
;; good
(defmacro when-not [test & body]
  `(when (not ~test)
     ~@body))

;; bad
(defmacro when-not [test & body]
  (list 'when (list 'not test)
        (cons 'do body)))
```

Write the desired call site first, then implement the macro. Break complex macros into helper functions. Keep macros as thin syntactic sugar over a plain function:

```clojure
;; good: macro is thin sugar over a function
(defn perform-with-timing [label thunk]
  (let [start (System/nanoTime)
        result (thunk)
        elapsed (- (System/nanoTime) start)]
    (println label "took" elapsed "ns")
    result))

(defmacro with-timing [label & body]
  `(perform-with-timing ~label (fn [] ~@body)))
```

(`System/nanoTime` here is for illustration only; host skills cover platform-specific timing.)
