# State and Dispatch

Part of the `clojure` skill. This host-neutral guidance applies to `.clj`, `.cljs`, `.cljc`, and `.cljd` code. Host skills cover refs, agents, STM, and other runtime-specific concurrency primitives.

## State Management

This section covers atoms only. Refs, agents, STM, `io!`, and `alter-var-root` are JVM-only and live in [clojure-jvm-skills](https://github.com/brackendev/clojure-jvm-skills).

### No def inside defn

```clojure
;; bad
(defn foo []
  (def x 5)
  ...)

;; good: use let or atoms
(defn foo []
  (let [x 5]
    ...))
```

### swap! over reset!

Prefer `swap!` to derive the new value from the old:

```clojure
(def a (atom 0))

;; good
(swap! a + 5)

;; not as good: ignores current value
(reset! a 5)
```

## Dispatch

Choose the simplest dispatch mechanism that fits:

```clojure
;; Closed set of cases: use cond, case, or a map lookup
(defn area [{:keys [type] :as shape}]
  (case type
    :circle (* 3.141592653589793 (:radius shape) (:radius shape))
    :rect   (* (:width shape) (:height shape))))

;; Open set with single dispatch axis: use multimethods
(defmulti area :type)
(defmethod area :circle [{:keys [radius]}] (* 3.141592653589793 radius radius))
(defmethod area :rect [{:keys [width height]}] (* width height))

;; Performance-critical polymorphism or type-based dispatch: use protocols
(defprotocol Shape
  (area [this]))

(defrecord Circle [radius]
  Shape
  (area [_] (* 3.141592653589793 radius radius)))
```

Protocols require a concrete type to dispatch on. Multimethods dispatch on arbitrary functions of the arguments. For most application code, a `case` or map lookup on a keyword is sufficient and simpler than either.
