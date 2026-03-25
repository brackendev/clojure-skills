---
name: clojure
description: >-
  Use when writing, editing, reviewing, or discussing Clojure code. Triggers: .clj
  files, deps.edn projects, project.clj, build.clj, clojure.test, REPL usage, or
  any mention of Clojure. Covers idiomatic style, naming conventions, threading
  macros, collection idioms, state management, and common anti-patterns.
user-invocable: false
---

# Clojure

Clojure is a dynamic, functional Lisp dialect targeting the JVM. This skill covers idiomatic style, naming conventions, and patterns that are commonly violated.

## When to Apply

Apply this knowledge when:

- Working with `.clj` files
- A `deps.edn` contains Clojure dependencies (but not `tensegritics/clojuredart`)
- A `project.clj` (Leiningen) or `build.clj` (tools.build) file is present
- The user mentions Clojure (not ClojureScript or ClojureDart)

## MCP Integration

This skill is designed to work with [clojure-mcp](https://github.com/bhauman/clojure-mcp), an MCP server for REPL-driven Clojure development. When clojure-mcp is available, use its tools for REPL evaluation, namespace reloading, and file operations instead of shell commands. The REPL practices in this skill apply when using clojure-mcp.

If clojure-mcp tools are not available and an `:nrepl` alias exists, start nREPL in the background with `clj -M:nrepl &` and restart the session so the MCP server can connect.

## CLI

```bash
clj                          # Start a REPL
clj -M:alias                 # Run with alias
clj -X:alias fn-name         # Execute a function
clj -T:build task            # Run a tools.build task
clj -Sdeps '{:deps {...}}'   # Add inline dependencies
```

## Naming Conventions

```clojure
;; Predicates end with ?
(defn palindrome? [s] ...)

;; Not Java-style or Common Lisp-style
(defn is-palindrome [s] ...)   ;; bad
(defn palindrome-p [s] ...)    ;; bad

;; Conversion functions use ->
(defn f->c [f] ...)

;; Not -to- or other separators
(defn f-to-c [f] ...)          ;; bad

;; Dynamic vars use *earmuffs*
(def ^:dynamic *db-connection* nil)

(def ^:dynamic db-connection nil)  ;; bad

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

## Function Design

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
     (map (partial * 2)))
```

Omit parentheses around forms that take no extra arguments:

```clojure
;; good
(-> x fizz :foo first frob)

;; bad: unnecessary parens
(-> x (fizz) (:foo) (first) (frob))

;; good: parens required when passing extra args
(-> x
    (fizz a b)
    :foo
    first
    (frob x y))
```

### Conditional Threading

`cond->` and `cond->>` apply transformations only when conditions are true:

```clojure
(cond-> m
  name    (assoc :name name)
  age     (assoc :age age)
  admin?  (assoc :role :admin))

(cond->> users
  active-only?  (filter :active)
  limit         (take limit))
```

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

### No commas

```clojure
;; good
[1 2 3]

;; bad
[1, 2, 3]
```

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

### alter-var-root over re-def

```clojure
;; good
(def thing 1)
(alter-var-root #'thing (constantly nil))

;; bad
(def thing 1)
(def thing nil)
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

### No atoms inside STM transactions

```clojure
;; good: atom update after transaction
(dosync
  (alter account-a - 100)
  (alter account-b + 100))
(swap! transfer-log conj {:from :a :to :b :amount 100})

;; bad: swap! retries on every STM retry
(dosync
  (alter account-a - 100)
  (alter account-b + 100)
  (swap! transfer-log conj {:from :a :to :b :amount 100}))
```

## Strings

Prefer `clojure.string` functions over Java interop:

```clojure
;; good
(clojure.string/upper-case "bruce")

;; bad
(.toUpperCase "bruce")
```

## Java Interop

Use sugared syntax:

```clojure
;; good
(java.util.ArrayList. 100)
(Math/pow 2 10)
(.substring "hello" 1 3)
Integer/MAX_VALUE

;; bad
(new java.util.ArrayList 100)
(. Math pow 2 10)
(. "hello" substring 1 3)
(. Integer MAX_VALUE)
```

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

## Formatting

- 2-space indentation, no tabs
- 80 characters per line (120 maximum)
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

## Namespace Conventions

```clojure
(ns my-app.core
  (:require
   [clojure.string :as str]
   [clojure.set :as set]
   [my-app.db :as db]
   [my-app.util :as util])
  (:import
   [java.time LocalDate Instant]
   [java.util UUID]))
```

- `:require` over `:use`
- `:as` aliases over `:refer :all`
- `:refer` only specific vars when needed
- Group requires: clojure.core libs, third-party libs, project namespaces
- Hyphenated namespace segments map to underscored file paths: `my-app.core` maps to `src/my_app/core.clj`

## Project Structure

```
my-project/
  deps.edn              # Dependencies and aliases
  build.clj             # tools.build tasks (optional)
  .cljfmt.edn           # cljfmt formatting rules (optional)
  src/
    my_app/
      core.clj           # Entry point or main namespace
      db.clj             # Feature modules
  test/
    my_app/
      core_test.clj      # Tests mirror src structure
  resources/             # Non-code resources
```

## deps.edn Configuration

```clojure
{:paths ["src" "resources"]
 :deps  {org.clojure/clojure {:mvn/version "1.12.0"}}
 :aliases
 {:dev     {:extra-paths ["dev"]
            :extra-deps  {}}
  :test    {:extra-paths ["test"]
            :extra-deps  {io.github.cognitect-labs/test-runner
                          {:git/tag "v0.5.1" :git/sha "dfb30dd"}}
            :main-opts   ["-m" "cognitect.test-runner"]
            :exec-fn     cognitect.test-runner.api/test}
  :build   {:deps        {io.github.clojure/tools.build
                          {:git/tag "v0.10.5" :git/sha "2a21b7a"}}
            :ns-default  build}
  :cljfmt  {:extra-deps  {dev.weavejester/cljfmt {:mvn/version "0.13.0"}}
            :main-opts   ["-m" "cljfmt.main"]}}}
```

## Tooling

| Tool | Command | Purpose |
|------|---------|---------|
| Clojure CLI | `clj` | REPL, run, execute |
| tools.build | `clj -T:build task` | Build tasks (uberjar, etc.) |
| clj-kondo | `clj-kondo --lint src` | Lint `.clj` files |
| cljfmt | `clj -M:cljfmt fix` | Format `.clj` files |
| test-runner | `clj -X:test` | Run tests |

## REPL

Always reload namespaces with the `:reload` flag to ensure you are working with the latest code:

```clojure
(require '[my-app.core] :reload)
```

Switch into the namespace being worked on:

```clojure
(in-ns 'my-app.core)
```

Keep function and namespace references fully qualified when crossing namespace boundaries:

```clojure
;; good: fully qualified across namespaces
(my-app.db/find-user id)

;; bad: unqualified, relies on use/refer
(find-user id)
```

Always reload namespaces before running tests:

```clojure
(require '[my-app.core-test] :reload)
(clojure.test/run-tests 'my-app.core-test)
```

For full test suite runs, prefer the CLI:

```bash
clj -X:test
```

## Key Rules

1. **Prefer `clojure.string` over Java interop.** Use `str/upper-case` not `.toUpperCase`.
2. **Use keywords as map lookup functions.** `(:name m)` not `(get m :name)`.
3. **Use sets as predicates.** `(remove #{1} coll)` not `(remove #(= % 1) coll)`.
4. **Use `seq` for empty checks.** `(when (seq s) ...)` not `(when-not (empty? s) ...)`.
5. **Use `when` for single-branch side effects.** Reserve `if` for two branches.
6. **Use `condp` or `case` over repeated `=` in `cond`.** Reduces duplication.
7. **No `def` inside `defn`.** Use `let` for local bindings.
8. **No commas in collection literals.** Whitespace is the separator.
9. **Prefer vectors over quoted lists.** `[1 2 3]` not `'(1 2 3)`.
10. **Do not write macros when functions work.** Macros complicate debugging and composition.
