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
(defn perform-with-label [label thunk]
  (println "running" label)
  (time (thunk)))

(defmacro with-label [label & body]
  `(perform-with-label ~label (fn [] ~@body)))
```

`time` exists on every host, so this example runs unchanged on the JVM, in ClojureScript, and in ClojureDart.

### Evaluate each argument at most once (Iron Law CLJ-7)

Every place a macro unquotes an argument becomes a copy of that expression in the expansion. An argument unquoted twice is evaluated twice, along with its side effects:

```clojure
;; bad: (twice (swap! n inc)) increments n two times
(defmacro twice [x] `(+ ~x ~x))

;; good: bind the argument once with an auto-gensym
(defmacro twice [x] `(let [v# ~x] (+ v# v#)))
```

A macro may evaluate an argument zero times, as `when` does when its test fails, or repeatedly when repetition is its purpose, as in a loop body. What it must not do is copy an argument into the expansion more than once by accident.
