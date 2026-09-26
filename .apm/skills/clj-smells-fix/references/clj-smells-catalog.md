# clj-smells Catalog Digest

Pinned to upstream commit `d1ae1896f518e94f3d4f9257b451d89473b94e1b` (2026-01-19).
Source: <https://github.com/nufuturo-ufcg/clj-smells-catalog>

The catalog documents 35 Clojure-specific code smells. This digest groups them by detection method and provides default severity guidance, short examples, and false-positive notes for review use. Severity is a starting point; assign the final tier based on impact in context.

---

## Stage 1: Detected by clj-kondo (with overlay)

These smells are caught by `clj-kondo` when `references/clj-kondo-overlay.edn` is active. The Stage 1 pass converts each clj-kondo finding to a review entry.

### Implicit Namespace Dependencies

Default severity: `SMELL`.
Pattern: `(:require [some.ns :refer :all])`.
Why: namespace pollution, symbol ambiguity, prevents reliable static analysis.
Fix: use `:as` aliases or `:refer` with an explicit short list.
clj-kondo linter: `:refer-all`.

### Excessive Refers

Default severity: `HINT`.
Pattern: `(:require [some.ns :refer [a b c d e f g h i j ...]])` with many symbols.
Why: callers cannot tell which namespace defines a given symbol.
Fix: prefer `:as` aliases; reserve `:refer` for two or three high-traffic symbols.
clj-kondo linter: `:use`, which the overlay enables to flag `(:use ...)` forms. Excessive explicit refers have no Stage 1 linter and fall through to Stage 2.

### Redundant `do` block

Default severity: `HINT`.
Pattern: `(when pred (do a b c))`. Forms like `when`, `let`, `try`, and function bodies already wrap an implicit `do`.
Fix: remove the inner `(do ...)`.
clj-kondo linter: `:redundant-do`.

### Nested Forms

Default severity: `HINT`.
Pattern: `(let [a 1] (let [b 2] ...))` or `(when-let [x ...] (when-let [y ...] ...))`.
Fix: combine bindings (`let` accepts a vector of multiple bindings; chain with `when-some`/`if-let` or refactor to `some->`).
clj-kondo linter: `:redundant-let`.

### Direct usage of `clojure.lang.RT`

Default severity: `SMELL` (escalate to `DEFECT` when used to bypass intended public API).
Pattern: `(clojure.lang.RT/...)`.
Why: `RT` is internal; methods are not part of Clojure's public API and may break across versions.
Fix: use the public `clojure.core` function or a documented Java interop call.
clj-kondo linter: `:discouraged-var` configured for `clojure.lang.RT`.

---

## Stage 2: LLM-Detected Smells

clj-kondo cannot detect these by default. Evaluate each candidate against the actual code, not against assumptions about what the code might do.

### Macros and Metaprogramming

#### Unnecessary Macros

Default severity: `SMELL`.
Pattern: `(defmacro foo [x] `(do ~x))` or any macro whose body could be a function.
Fix: rewrite as `defn`.
False positive: macros that capture forms, control evaluation, or generate `def`/`defn` are correct.

#### Multiple Evaluation in Macros

Default severity: `DEFECT`.
Pattern: `(defmacro twice [x] `(+ ~x ~x))` -- argument inserted twice without binding.
Why: side-effecting arguments execute multiple times.
Fix: bind once with `gensym` or `let` inside the syntax-quoted form: `` `(let [x# ~x] (+ x# x#)) ``.

### State Management

#### Immutability Violation

Default severity: `SMELL`.
Pattern: mutable Java collections (`java.util.HashMap`) or unboxed `volatile!` used for ordinary application state.
Fix: use Clojure's persistent collections; reach for `atom`/`ref`/`agent` only when coordinated mutation is required.

#### Misuse of Dynamic Scope

Default severity: `SMELL`.
Pattern: `^:dynamic` vars used to thread ordinary application data through call stacks.
Why: hidden dependencies, action-at-a-distance, hostile to parallelism.
Fix: pass data as explicit function arguments. Reserve `binding` for cross-cutting concerns (logging context, request context).
False positive: `binding` over `clojure.test/*report-counters*`-style infrastructure is intended use.

#### Nested Atoms

Default severity: `DEFECT`.
Pattern: `(atom {:cache (atom {})})` -- atom referencing another atom.
Why: prevents consistent state snapshots; coordinated updates require manual locking.
Fix: flatten to a single atom holding nested data, or use `ref`s with `dosync`.

#### Dynamically-Scoped Singleton Resource

Default severity: `SMELL`.
Pattern: `(def ^:dynamic *db* nil)` for a database connection or similar resource.
Why: prevents thread dispatch, couples callers to a global, breaks testing.
Fix: pass the resource as an argument or via a system map (Component/Integrant/Mount).

### Concurrency

#### Blocking Inside Go

Default severity: `DEFECT`.
Pattern: `(go (Thread/sleep 1000))`, `(go (slurp url))`, `(go (<!! ch))` -- any blocking call inside a `go` block.
Why: `go` blocks share a fixed thread pool; blocking starves it.
Fix: use `thread` for blocking work, or move the blocking call outside the `go` block.

#### Misuse of Channel Closing Semantics

Default severity: `SMELL`.
Pattern: sentinel values like `(>! ch :done)` followed by consumers checking `(= msg :done)`.
Fix: close the channel with `(close! ch)`. Consumers detect closure when `<!`/`<!!` returns `nil`.

#### Overengineering with `core.async`

Default severity: `SMELL`.
Pattern: channels used for one-shot results where `promise`, `future`, or a plain function would do.
Fix: use the simplest mechanism. Channels are for fan-out, backpressure, and pipelines.
False positive: legitimate fan-out, batching, or backpressure scenarios.

### Namespace and Loading

#### Namespace Load Side Effects

Default severity: `SMELL` (`DEFECT` when the side effect mutates external state).
Pattern: `(println ...)`, `(reset! global-atom ...)`, or other side effects at the namespace top level (outside `defn`).
Fix: wrap in a function and call it explicitly from `-main` or a system component.

#### Single-segment Namespace

Default severity: `SMELL`.
Pattern: `(ns digest)` instead of `(ns my-app.digest)`.
Why: collision risk and ecosystem convention violation.
Fix: nest under a project namespace.

#### Relying on Load-Time Side Effects

Default severity: `DEFECT`.
Pattern: behavior that depends on `require` order, top-level `def` reads from another namespace's mutable state, or `defmethod` registration timing.
Fix: make initialization explicit and idempotent.

#### Monolithic Namespace Split

Default severity: `SMELL`.
Pattern: multiple files using `(load "...")` and `(in-ns 'foo.bar)` to share one logical namespace.
Why: breaks static analysis, dependency resolution, and REPL reloading.
Fix: split into separate namespaces with clear `:require` relationships.

### Idioms and Style

#### Improper Emptiness Check

Default severity: `HINT`.
Pattern: `(when (not (empty? coll)) ...)` or `(if (= 0 (count coll)) ...)`.
Fix: use `(when (seq coll) ...)`. `seq` returns `nil` for empty collections.

#### Unnecessary `into`

Default severity: `HINT`.
Pattern: `(into [] coll)` for simple type conversion.
Fix: use `vec`. Apply `into` when adding elements or transducing.

#### Verbose Checks

Default severity: `HINT`.
Pattern: `(= 0 x)`, `(> x 0)`, `(< x 0)`, `(+ x 1)`, `(- x 1)`.
Fix: `zero?`, `pos?`, `neg?`, `inc`, `dec`.

#### Thread Ignorance

Default severity: `HINT`.
Pattern: deeply nested function calls or verbose intermediate `let` bindings where `->` or `->>` would clarify the data flow.
Fix: rewrite with the appropriate threading macro.
False positive: code where intermediate bindings carry meaningful names.

#### Unnecessary Laziness

Default severity: `HINT`.
Pattern: `(map f coll)` where the result is immediately realized via `(into [] ...)`, `(reduce conj [] ...)`, or `doall`.
Fix: use the eager `mapv`, `filterv`, or `reduce`.

#### Misused Threading

Default severity: `HINT`.
Pattern: `(-> x (...) (assoc :k v) keys count)` -- threading where the data type changes fundamentally at each step.
Fix: split into stages with intermediate names, or restructure to keep one threading shape.

#### Conditional Build-Up

Default severity: `SMELL`.
Pattern: incremental construction via cascading `let` + `if` + `assoc`:

```clojure
(let [m {:a 1}
      m (if x (assoc m :b 2) m)
      m (if y (assoc m :c 3) m)]
  m)
```

Fix: use `cond->`:

```clojure
(cond-> {:a 1}
  x (assoc :b 2)
  y (assoc :c 3))
```

### Records, Protocols, Multimethods

#### Non-Idiomatic Record Construction

Default severity: `SMELL`.
Pattern: `(MyRecord. v1 v2)` or `(->MyRecord v1 v2)` when field count exceeds three or fields are easily reordered.
Why: positional construction breaks silently if the record's fields are reordered.
Fix: use `(map->MyRecord {:field-a v1 :field-b v2})`.
False positive: records with one or two fields where positional order is obvious.

#### Marker Protocol

Default severity: `SMELL`.
Pattern: `(defprotocol Tagged)` with no methods, used only as a type identifier.
Fix: use a metadata key, namespaced keyword on the data, or `derive`/`isa?` for hierarchy.

#### Private Multimethods

Default severity: `SMELL`.
Pattern: `(defn- multi-fn ...)` or `^:private` on a `defmulti`/`defmethod`.
Why: defeats open polymorphism; external code cannot extend.
Fix: make the multimethod public, or use a private function with `case`/`cond` if open dispatch is not needed.

### Functions and Parameters

#### Non-Idiomatic Parameter Binding

Default severity: `HINT`.
Pattern: `(defn foo [x & [y]])` -- using rest-args destructuring for an optional parameter.
Fix: use multiple arities or an options map.

```clojure
(defn foo
  ([x] (foo x default-y))
  ([x y] ...))
```

#### Production `doall`

Default severity: `SMELL`.
Pattern: `(doall (map f huge-coll))` in production code paths.
Why: defeats laziness, may cause memory spikes for large sequences.
Fix: use `mapv`/`filterv`/`reduce` for eager work, transducers for streaming, or `run!`/`doseq` for side effects.
False positive: REPL utilities, one-off scripts, and small bounded sequences where eager realization is the explicit intent.

### Resources and I/O

#### Unmanaged Resource I/O

Default severity: `DEFECT`.
Pattern: `(let [r (clojure.java.io/reader path)] ...)` without `with-open`, or any closeable resource left without explicit `.close`.
Why: file handles, sockets, and database connections leak.
Fix: use `with-open`:

```clojure
(with-open [r (clojure.java.io/reader path)]
  (slurp r))
```

### Data

#### Map With Nil Values

Default severity: `SMELL`.
Pattern: `{:a 1 :b nil}` returned from a function or stored in state.
Why: callers cannot distinguish "key absent" from "key explicitly nil".
Fix: omit `nil`-valued keys (`cond->`, `dissoc`, or filter the map).
False positive: maps where `nil` is a meaningful value distinct from "absent" (rare but legitimate).

#### Case with Non-Literal Test Values

Default severity: `DEFECT`.
Pattern: `(case x sym-bound-to-1 :one ...)` where `sym-bound-to-1` is a runtime value.
Why: `case` matches against the symbol literally, not its value, leading to silent misses.
Fix: use `condp =` or a `cond` with explicit equality.

### Reagent / re-frame Specific

#### Refs in Dependency Vector

Default severity: `DEFECT` (only in Reagent/re-frame).
Pattern: passing a Reagent ratom to a `:depends-on`/dependency vector instead of dereferencing it.
Why: causes unexpected re-runs or misses updates.
Fix: dereference where the value is consumed (`@my-ref`), not where the ref is registered.
False positive: outside Reagent/re-frame, this smell does not apply. Skip when reviewing plain Clojure or non-reactive ClojureScript.
