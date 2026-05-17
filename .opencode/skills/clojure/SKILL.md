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
- [clojuredart-skills](https://github.com/brackendev/clojuredart-skills) -- ClojureDart on Flutter: Dart interop, type hints and nullability, `cljd.flutter` directives, async, FFI, REPL.
- [biff-skills](https://github.com/brackendev/biff-skills) -- the [Biff](https://biffweb.com/) web framework on the JVM.

This skill does not duplicate that material. When in JVM, ClojureScript, or ClojureDart context, defer to the host skill for its named domains and keep using this skill for everything else.

## REPL-Driven Development

REPL-driven development is the primary workflow for Clojure family code. Prefer evaluating small forms in a running REPL before changing larger code paths; let file edits follow from working REPL code, not the other way around. When no REPL is reachable, start one before continuing — the host skill names the dialect-specific command (nREPL on the JVM, the socket REPL on ClojureDart).

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
(if-not pred (foo))
(when-not pred (foo) (bar))

;; bad
(if (not pred) (foo))
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

Consider function pre and post conditions as an alternative to manual checks:

```clojure
;; good
(defn foo [x]
  {:pre [(pos? x)]}
  (bar x))

;; bad
(defn foo [x]
  (if (pos? x)
    (bar x)
    (throw (ex-info "x must be positive" {:value x}))))
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

## Threading Macros

Use `->` when the argument threads into the first position. Use `->>` when it threads into the last position:

```clojure
;; -> thread-first
(-> [1 2 3]
    reverse
    (conj 4)
    prn)

;; ->> thread-last
(->> (range 1 10)
     (filter even?)
     (map #(* 2 %)))
```

Omit parentheses around forms that take no extra arguments:

```clojure
;; good
(-> x fizz :foo first frob)

;; bad: unnecessary parens
(-> x (fizz) (:foo) (first) (frob))
```

### Conditional Threading

`cond->` and `cond->>` apply transformations only when conditions are true:

```clojure
(cond-> m
  name    (assoc :name name)
  age     (assoc :age age)
  admin?  (assoc :role :admin))
```

### Nil-safe Threading

`some->` and `some->>` short-circuit on `nil`. Use them when intermediate steps may return `nil`:

```clojure
;; good: stops if any step returns nil
(some-> user :address :city str/upper-case)

;; bad: NPE risk if :address is nil
(-> user :address :city str/upper-case)
```

`if-some` and `when-some` bind non-nil values (unlike `if-let`/`when-let`, which test for logical truth and treat `false` as failure):

```clojure
;; good: handles false correctly
(when-some [v (get config :enabled)]
  (println "enabled:" v))

;; bad: treats false as missing
(when-let [v (get config :enabled)]
  (println "enabled:" v))
```

## Laziness

Clojure sequences are lazy by default. `map`, `filter`, `remove`, and `take` return lazy sequences. When side effects matter, unrealized lazy sequences are a source of bugs.

```clojure
;; bad: map returns a lazy seq, side effects may never execute
(map println ["a" "b" "c"])

;; good: run! forces side effects, returns nil
(run! println ["a" "b" "c"])

;; good: doseq for side-effecting iteration
(doseq [x ["a" "b" "c"]]
  (println x))

;; good: mapv returns an eager vector
(mapv process items)

;; good: doall forces a lazy seq when you need the realized collection
(doall (map transform items))
```

When building a result, prefer eager functions (`mapv`, `filterv`, `reduce`) or transducers. When composing transformations for later consumption, lazy sequences are appropriate.

## Variable Binding

Minimize `let` bindings. Inline values used only once:

```clojure
;; good: inline single-use value
(str/upper-case (get-name user))

;; bad: unnecessary binding
(let [name (get-name user)]
  (str/upper-case name))
```

Use threading macros to eliminate intermediate bindings:

```clojure
;; good
(->> users
     (filter active?)
     (map :name)
     sort)

;; bad
(let [active (filter active? users)
      names  (map :name active)]
  (sort names))
```

Use destructuring in function parameters instead of separate `let` bindings:

```clojure
;; good
(defn process [{:keys [name age]}]
  (str name " is " age))

;; bad
(defn process [person]
  (let [name (:name person)
        age  (:age person)]
    (str name " is " age)))
```

## Control Flow

Track actual values instead of boolean flags:

```clojure
;; good: return the found value or nil
(defn find-user [id users]
  (first (filter #(= id (:id %)) users)))

;; bad: boolean flag with separate lookup
(defn user-exists? [id users]
  (some #(= id (:id %)) users))
```

Return `nil` for "not found" conditions rather than wrapper objects:

```clojure
;; good
(when-let [user (find-user id)]
  (process user))

;; bad
(let [result (find-user id)]
  (if (:found? result)
    (process (:user result))
    (handle-missing)))
```

## Collections

### Keywords as functions

```clojure
(def m {:name "Bruce" :age 30})

;; good
(:name m)

;; bad: verbose
(get m :name)

;; bad: NPE risk if m is nil
(m :name)
```

### Sets as predicates

```clojure
;; good
(remove #{1} [0 1 2 3 4 5])
(count (filter #{\a \e \i \o \u} "mary had a little lamb"))

;; bad
(remove #(= % 1) [0 1 2 3 4 5])
```

### seq for empty checks

Use `seq` for nil punning instead of `empty?`:

```clojure
;; good
(when (seq s)
  (prn (first s)))

;; bad
(when-not (empty? s)
  (prn (first s)))
```

### Prefer vectors

```clojure
;; good
[1 2 3]

;; bad: quoted list as container
'(1 2 3)
```

### Keyword keys

```clojure
;; good
{:name "Bruce" :age 30}

;; bad
{"name" "Bruce" "age" 30}
```

### Namespaced keys

Use namespaced keywords to signal parsed domain data and prevent key collisions:

```clojure
;; good: namespaced keys mark domain-specific data
{:order/id 123
 :order/total 45.99
 :order/status :pending}

;; bad: bare keys collide across domains
{:id 123
 :total 45.99
 :status :pending}
```

Use a schema library available on the target runtime (`clojure.spec.alpha` and Malli on JVM and ClojureScript) at system boundaries to parse raw input into domain maps. Downstream functions receive conformed data and do not re-validate. The host skill names the runtime-specific options.

### Commas in collections

No commas in sequential collection literals (vectors, lists). Commas in maps are optional for readability:

```clojure
;; good
[1 2 3]
{:name "Bruce" :age 30}
{:name "Bruce", :age 30}  ;; also good in maps

;; bad
[1, 2, 3]
```

### vec over into

```clojure
;; good
(vec some-seq)

;; bad
(into [] some-seq)
```

### list* over nested cons

```clojure
;; good
(list* 1 2 3 [4 5])

;; bad
(cons 1 (cons 2 (cons 3 [4 5])))
```

### Prefer destructuring over index access

```clojure
;; good
(let [[x y] point]
  (process x y))

;; bad: fragile, says nothing about what index 0 and 1 mean
(process (nth point 0) (nth point 1))
```

### Record constructors

Use auto-generated factory functions, not interop syntax:

```clojure
(defrecord Foo [a b])

;; good
(->Foo 1 2)
(map->Foo {:a 3 :b 4})

;; bad: interop form
(Foo. 1 2)
```

The factory functions exist on every host that ships `defrecord` (JVM Clojure, ClojureScript, ClojureDart). ClojureDart raises the stakes further: the raw interop form requires three extra arguments (`nil {} -1` for meta, extmap, hash) that the factory functions supply automatically.

### Numeric idioms

```clojure
;; good
(inc x)
(dec x)
(pos? x)
(neg? x)
(zero? x)

;; bad
(+ x 1)
(- x 1)
(> x 0)
(< x 0)
(= x 0)
```

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

## Strings

Prefer `clojure.string` functions over host string interop:

```clojure
;; good
(clojure.string/upper-case "bruce")
```

The host skill covers the host's own string namespace where it adds anything beyond `clojure.string`.

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

## Formatting

- 2-space indentation, no tabs
- Aim for 80 characters per line (120 maximum)
- Vertically align `let` bindings and map keys
- Single blank line between top-level forms
- No blank lines within function bodies
- Gather closing parentheses on the same line

```clojure
;; good
(defn process [items]
  (let [filtered (filter valid? items)
        mapped   (map transform filtered)]
    (reduce combine mapped)))

;; bad: closing parens on separate lines
(defn process [items]
  (let [filtered (filter valid? items)
        mapped   (map transform filtered)]
    (reduce combine mapped)
  )
)
```

### Comments

- `;;;;` for section headings
- `;;;` for top-level comments outside definitions
- `;;` for code fragment comments (before the line they describe)
- `;` for margin (inline) comments

Use `#_` to comment out forms instead of `;`:

```clojure
;; good
(+ foo #_(bar x) delta)

;; bad
(+ foo
   ;; (bar x)
   delta)
```

## Namespace Conventions

```clojure
(ns my-app.core
  (:require
   [clojure.string :as str]
   [clojure.set :as set]
   [my-app.db :as db]
   [my-app.util :as util]))
```

- `:require` over `:use`
- `:as` aliases over `:refer :all`
- `:refer` only specific vars when needed
- Sort requires alphabetically
- Group requires: clojure.core libs, third-party libs, project namespaces
- Avoid single-segment namespaces (use `my-app.core`, not `my-app`)
- Hyphenated namespace segments map to underscored file paths: `my-app.core` maps to `src/my_app/core.clj` on the JVM, `src/my_app/core.cljs` for ClojureScript, `src/my_app/core.cljd` for ClojureDart

Host-specific import forms (`(:import ...)` on the JVM, Dart package require strings in ClojureDart, `goog` imports in ClojureScript) live in the host skill.

Use idiomatic aliases consistently across the project. Cross-host aliases:

| Namespace | Alias |
|-----------|-------|
| `clojure.string` | `str` |
| `clojure.set` | `set` |
| `clojure.walk` | `walk` |
| `clojure.edn` | `edn` |
| `clojure.pprint` | `pp` |

Host-specific aliases (`clojure.java.io`, `clojure.math`, `clojure.tools.logging`, `clojure.spec.alpha`, `clojure.core.async`) live in the host skill.

## Privacy and Metadata

Use `defn-` for private functions and `^:private` for private vars:

```clojure
;; good
(defn- helper [x] ...)
(def ^:private internal-state (atom {}))

;; bad: verbose
(defn ^:private helper [x] ...)

;; bad: not private
(defn helper [x] ...)
```

Use compact metadata notation for boolean flags:

```clojure
;; good
(def ^:private a 5)
(def ^:dynamic *config* {})

;; bad: verbose
(def ^{:private true} a 5)
(def ^{:dynamic true} *config* {})
```

Access private vars in tests with `@#'some.ns/var`.

## Exception Handling

Prefer `ex-info` for data-carrying exceptions across every host:

```clojure
;; good
(throw (ex-info "Invalid input" {:value x :reason :negative}))
```

Host skills cover host-typed exceptions, resource cleanup (`with-open` on the JVM), and the catch-all rules (no `Throwable` on the JVM, `:default` in ClojureScript, Dart `Exception` in ClojureDart).

## Testing

Name test namespaces `yourproject.something-test`. Name tests `something-test`:

```clojure
;; good
(deftest user-validation-test ...)

;; bad
(deftest test-user-validation ...)
(deftest user-validation-tests ...)
```

Use `testing` blocks for context in failure output:

```clojure
(deftest user-validation-test
  (testing "rejects blank names"
    (is (not (valid? {:name ""}))))
  (testing "accepts valid names"
    (is (valid? {:name "Bruce"}))))
```

Use `are` for tabular tests:

```clojure
(deftest palindrome?-test
  (are [s expected] (= expected (palindrome? s))
    "racecar" true
    "hello"   false
    "madam"   true
    ""        true))
```

`thrown?` and `thrown-with-msg?` accept host-specific exception types. Examples and host-specific guidance (JVM types, ClojureScript `:default`, ClojureDart `Exception`) live in the host skill.

Prefer passing dependencies as function arguments. `with-redefs` exists on the JVM and in ClojureScript but not in ClojureDart; the JVM and ClojureScript skills cover the trade-offs there.

## Docstrings

Place docstrings after the function name, not after the argument vector. Start with a complete, capitalized sentence on the first line. Wrap parameter references in backticks:

```clojure
;; good
(defn frobnitz
  "Computes the frobnitz of `x`.
  Returns nil when `x` is negative."
  [x]
  ...)

;; bad: docstring after arg vector (becomes a string in the body)
(defn frobnitz [x]
  "Computes the frobnitz of x."
  ...)
```
