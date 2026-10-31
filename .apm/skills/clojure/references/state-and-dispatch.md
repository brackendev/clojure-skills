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

### No side effects inside swap! (Iron Law CLJ-3)

The `swap!` docstring says the function "may be called multiple times, and thus should be free of side effects." Compute the new value inside the function, and act on the result after `swap!` returns:

```clojure
;; bad: the message can be sent more than once
(swap! state (fn [s] (send-email! (:user s)) (update s :sent inc)))

;; good: swap! returns the new value, and the effect runs once
(let [s (swap! state update :sent inc)]
  (send-email! (:user s)))
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

Never read an atom with `@` and then write a value computed from that read with `reset!`. Another thread, or on a single-threaded host another callback, can change the atom between the read and the write, and the write silently discards that change. Compute the new value inside `swap!`, or use `compare-and-set!` when the write must happen only if the value is still the one that was read.

```clojure
;; bad: check-then-act; a change between the deref and the reset! is lost
(when (< @counter limit)
  (reset! counter (inc @counter)))

;; good: the check and the update happen together
(swap! counter (fn [n] (if (< n limit) (inc n) n)))
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
