# Naming and Function Design

Part of the `clojure` skill. This host-neutral guidance applies to `.clj`, `.cljs`, `.cljc`, and `.cljd` code.

## Naming Conventions

```clojure
;; Predicates end with ?
(defn palindrome? [s] ...)

;; Not Java-style or Common Lisp-style
(defn is-palindrome [s] ...)   ;; bad
(defn palindrome-p [s] ...)    ;; bad

;; Side-effecting functions end with !
(defn save-user! [user]
  (db/insert! user))

(defn save-user [user] ...)    ;; bad: no ! warning

;; Conversion functions use ->
(defn f->c [f] ...)

;; Not -to- or other separators
(defn f-to-c [f] ...)          ;; bad

;; Dynamic vars use *earmuffs*
(def ^:dynamic *db-connection* nil)

(def ^:dynamic db-connection nil)  ;; bad

;; Constants use plain lisp-case, no special notation
(def max-size 10)

(def MAX-SIZE 10)              ;; bad: Java style
(def +max-size+ 10)            ;; bad: Common Lisp style

;; Protocols, records, and types use CapitalCase
(defprotocol Serializable ...)
(defrecord HttpRequest ...)
(deftype XMLParser ...)

(defrecord http-request ...)   ;; bad

;; Unused bindings use _ or _prefix
(let [[a b _ c] [1 2 3 4]]
  (println a b c))

(defn handler [request _response]
  (process request))
```

Do not shadow `clojure.core` names:

```clojure
;; bad: clojure.core/map must be fully qualified inside
(defn foo [map] ...)

;; good: use a descriptive name
(defn foo [m] ...)
```

### Idiomatic parameter names

Follow `clojure.core` conventions:

| Name | Meaning |
|------|---------|
| `f`, `g`, `h` | Function input |
| `n` | Integer (usually a size) |
| `i`, `index` | Integer index |
| `x`, `y` | Numbers |
| `xs` | Sequence |
| `m` | Map |
| `k`, `ks` | Key, keys |
| `v`, `vs` | Value, values |
| `s` | String |
| `coll` | Collection |
| `pred` | Predicate |
| `xf` | Transducer |
| `re` | Regular expression |
| `expr` | Expression (in macros) |
| `body` | Macro body |

## Function Design

### Function length

Aim for functions under 10 lines of code. Most functions work well under 5 lines. Extract helpers when a function grows beyond this.

```clojure
;; good: small, focused functions
(defn active-users [users]
  (filter :active? users))

(defn format-user [user]
  (str (:first-name user) " " (:last-name user)))

(defn active-user-names [users]
  (->> (active-users users)
       (map format-user)))
```

### when vs if

Use `when` for single-branch conditionals with side effects. Use `if` for two-branch decisions:

```clojure
;; good
(when pred
  (foo)
  (bar))

;; bad: if with do and no else branch
(if pred
  (do
    (foo)
    (bar)))
```

### if-not, when-not

```clojure
;; good
(if-not pred (foo) (bar))
(when-not pred (foo) (bar))

;; bad
(if (not pred) (foo) (bar))
(when (not pred) (foo) (bar))
```

### if-let, when-let

```clojure
;; good
(if-let [result (foo x)]
  (something-with result)
  (something-else))

(when-let [result (foo x)]
  (do-something-with result)
  (do-something-more-with result))

;; bad: separate let + if
(let [result (foo x)]
  (if result
    (something-with result)
    (something-else)))
```

### condp and case

Use `condp` when testing the same expression repeatedly. Use `case` for compile-time constants:

```clojure
;; bad
(cond
  (= x 10) :ten
  (= x 20) :twenty
  :else :dunno)

;; better
(condp = x
  10 :ten
  20 :twenty
  :dunno)

;; best for constants
(case x
  10 :ten
  20 :twenty
  30 :thirty
  :dunno)
```

### not=

```clojure
;; good
(not= foo bar)

;; bad
(not (= foo bar))
```

### Multiple arity

Order from fewest to most arguments. Lower arities delegate to higher:

```clojure
(defn foo
  ([x]
   (foo x 1))
  ([x y]
   (+ x y)))
```

### Avoid wrapping lambdas

```clojure
;; good
(filter even? (range 1 10))

;; bad: unnecessary wrapper
(filter #(even? %) (range 1 10))
```

### Higher-order functions over loop/recur

```clojure
;; good
(map inc [1 2 3 4 5])

;; bad: manual loop to do the same thing
(loop [xs [1 2 3 4 5] result []]
  (if (empty? xs)
    result
    (recur (rest xs) (conj result (inc (first xs))))))
```

### Positional parameter limit

Avoid more than three or four positional parameters. Use an options map instead:

```clojure
;; good
(defn create-user [{:keys [name email role admin?]}]
  ...)

;; bad: callers must remember exact order
(defn create-user [name email role admin?]
  ...)
```

### Pre/post conditions

Use pre and post conditions for internal invariants that indicate a programming error. They throw an assertion error and stop running when `*assert*` is false, so validate external input with `ex-info` instead:

```clojure
;; internal invariant
(defn foo [x]
  {:pre [(pos? x)]}
  (bar x))

;; input validation at a boundary
(defn parse-quantity [x]
  (if (pos? x)
    x
    (throw (ex-info "Quantity must be positive" {:value x}))))
```

### :else in cond

Use `:else` as the catch-all, not `true`:

```clojure
;; good
(cond
  (neg? n) "negative"
  (pos? n) "positive"
  :else "zero")

;; bad
(cond
  (neg? n) "negative"
  (pos? n) "positive"
  true "zero")
```

### Flexible comparisons

Leverage the variable arity of `<`, `>`, and related functions:

```clojure
;; good
(< 5 x 10)

;; bad
(and (> x 5) (< x 10))
```

### Function literals

Use `%` when there is one parameter, `%1` when there are multiple. Do not use function literals for multi-form bodies:

```clojure
;; good
#(inc %)
#(+ %1 %2)

;; bad
#(inc %1)
#(+ % %2)

;; good: multi-form body uses fn
(fn [x]
  (println x)
  (* x 2))

;; bad: do inside function literal
#(do (println %)
     (* % 2))
```

### Prefer anonymous functions over comp and partial

Anonymous functions are generally clearer about argument position than `comp` or `partial`:

```clojure
;; good
(map #(+ 5 %) (range 1 10))

;; not as good
(map (partial + 5) (range 1 10))
```

`comp` is a good fit when composing transducers:

```clojure
(def xf
  (comp
    (filter odd?)
    (map inc)
    (take 5)))
```

### Pure functions

Prefer pure functions over functions with side effects. Functions should do one thing and return useful values that callers can compose.
