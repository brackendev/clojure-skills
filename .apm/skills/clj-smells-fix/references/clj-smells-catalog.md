# clj-smells Catalog Digest

Pinned to upstream commit `51abf4baef3298454e88baf086b3413163c4629f` (2026-06-11).
Source: <https://github.com/nufuturo-ufcg/clj-smells-catalog>. Upstream keeps each smell's description in `README.md` and its cited sources and excerpts in `sources-and-excerpts.md`.

The catalog documents 34 Clojure-specific code smells. This digest groups them by detection method and provides default severity guidance, short examples, and false-positive notes for review use. The severities are defaults. The Severity Tiers section of `SKILL.md` defines how to assign the final tier.

---

## Stage 1: Detected by clj-kondo (with overlay)

clj-kondo reports these smells. `:redundant-do`, `:redundant-let`, `:refer-all`, `:use`, `:unused-binding`, and `:unused-namespace` are on by default, and `references/clj-kondo-overlay.edn` keeps them on when the project's configuration turns them off. Only the `clojure.lang.RT` check needs the overlay. The overlay also turns on `:case-symbol-test` at `:info`, which supplies evidence for the Stage 2 entry Case with Non-Literal Test Values. The Stage 1 pass converts each clj-kondo finding to a review entry.

### Implicit Namespace Dependencies

Default severity: `SMELL`.
Pattern: `(:require [some.ns :refer :all])`, `(:use some.ns)`, or code that calls a namespace it never requires because another library happens to load it.
Why: the origin of each symbol is hidden, names can collide, and static analysis cannot follow the dependency.
Fix: require the namespace explicitly, with an `:as` alias or a short explicit `:refer` list.
clj-kondo linters: `:refer-all` and `:use`. A namespace used without a require has no Stage 1 linter and falls through to Stage 2.

### Excessive Refers

Default severity: `HINT`.
Pattern: `(:require [some.ns :refer [a b c d e f g h i j ...]])` with many symbols.
Why: callers cannot tell which namespace defines a given symbol, and each referred name can collide with other code.
Fix: use an `:as` alias. The Clojure Style Guide prefers `:require :as` over `:require :refer`, and `:refer` over `:refer :all`.
clj-kondo linter: none. A long explicit `:refer` list falls through to Stage 2.

### Redundant `do` block

Default severity: `HINT`.
Pattern: `(when pred (do a b c))`. Forms like `when`, `let`, `fn`, `try`, and function bodies already wrap an implicit `do`.
Fix: remove the inner `(do ...)`.
clj-kondo linter: `:redundant-do`.

### Nested Forms

Default severity: `HINT`.
Pattern: binding or iteration forms nested without need: `(let [a 1] (let [b 2] ...))`, nested `doseq`, or `(when-let [x ...] (when-let [y ...] ...))`.
Fix: merge nested `let` or `doseq` bindings into one binding vector. `when-let`, `if-let`, `when-some`, and `if-some` accept exactly one binding, so flatten nested ones with `some->` when each step reads from the previous value, or extract a function.
clj-kondo linter: `:redundant-let`, which flags a `let` nested directly inside another `let` only. Other nested forms have no Stage 1 linter and fall through to Stage 2.

### Direct usage of `clojure.lang.RT`

Default severity: `SMELL` (escalate to `DEFECT` when used to bypass intended public API).
Pattern: `(clojure.lang.RT/...)`, for example `(iterator-seq (clojure.lang.RT/iter [1 2 3]))`.
Why: Alex Miller on Ask Clojure (https://ask.clojure.org/index.php/10303): "RT should be considered internal implementation and should not be called directly." Its methods are not part of Clojure's public API.
Fix: use the public `clojure.core` function that wraps the method, or a documented Java interop call. `RT/var` also creates the namespace and the var when they are missing, so replace it with `requiring-resolve` or `find-var` to look a var up.
clj-kondo linter: `:discouraged-java-method`, configured in the overlay for every `RT` method that a public `clojure.core` function wraps, for `RT/var`, and for `iter`, `classForName`, and `loadClassForName`, which published libraries call and which have no public equivalent. Each finding's message names the replacement. Calls to other `RT` methods, such as `RT/baseLoader`, fall through to Stage 2. `RT/loadLibrary` is not a finding: it has no `clojure.core` equivalent, and RT.java describes it as loading "a library in the System ClassLoader instead of Clojure's own".

---

## Stage 2: LLM-Detected Smells

The overlay adds no clj-kondo linter for these. Step 3 of the Fix Workflow in `SKILL.md` gives the rules for evaluating each candidate. A few of them have clj-kondo linters that are named in their entries, so the Stage 1 output can supply evidence for them.

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
Pattern: a function that redefines a var with `def` to change application state, as in `(defn update-country [c] (def countries (assoc countries (:name c) c)))`. Mutable Java collections (`java.util.HashMap`) or a `volatile!` used for shared application state are the same smell.
Fix: bind locals with `let` and return new values, and hold state that must change in an atom. `volatile!` is a box with non-atomic updates (`vswap!` "Non-atomically swaps"), meant for local state on one thread.
clj-kondo linter: `:inline-def`, on by default, reports a `def` inside a function body.

#### Misuse of Dynamic Scope

Default severity: `SMELL`.
Pattern: `^:dynamic` vars used to thread ordinary application data through call stacks.
Why: hidden dependencies, action-at-a-distance, hostile to parallelism.
Fix: pass data as explicit function arguments. Reserve `binding` for cross-cutting concerns (logging context, request context).
False positive: `binding` over `clojure.test/*report-counters*`-style infrastructure is intended use.

#### Nested Atoms

Default severity: `DEFECT`.
Pattern: `(atom {:cache (atom {})})` -- atom referencing another atom.
Why: updating the inner atom does not change the outer atom's value, so no single dereference gives a consistent snapshot of the whole state.
Fix: flatten to a single atom holding nested data. On the JVM, refs updated in one `dosync` fit when separate identities must change together.

#### Dynamically-Scoped Singleton Resource

Default severity: `SMELL`.
Pattern: `(def ^:dynamic *db* nil)` for a database connection or similar resource.
Why: it prevents dispatching work to other threads, breaks lazy sequences that outlive the `binding`, limits the program to one resource per thread, and hides the dependency.
Fix: pass the resource as an argument (Stuart Sierra: "Define API functions to take the resource as an argument"), or through a system map from Component or Integrant.

### Concurrency

#### Blocking Inside Go

Default severity: `DEFECT`.
Pattern: any blocking call inside a `go` block. On the JVM: `(go (Thread/sleep 1000))`, `(go (slurp url))`, `(go (<!! ch))`, `(go (alts!! [ch (timeout 1000)]))`. ClojureScript core.async has no `<!!`, `>!!`, or `alts!!`, so there the smell is a synchronous host call, such as a synchronous Node.js file read, inside `go`.
Why: the `go` docstring forbids it: blocking "risks depleting the fixed pool of go block threads, causing all go block processing to stop." core.async 1.9.865 on the JVM dispatches go blocks to a cached thread pool, so each blocked go block holds an operating system thread instead of parking. ClojureScript runs go blocks on the only JavaScript thread.
Fix: replace `<!!`, `>!!`, and `alts!!` with `<!`, `>!`, and `alts!` when the call is lexically inside the `go` body. Move blocking I/O to `io-thread`, which "may do blocking I/O but must not do extended computation" and uses a virtual thread when the JVM provides one, or to `thread`. Setting the JVM system property `clojure.core.async.go-checking` to `true` during development makes core.async blocking operations throw in go block threads.

#### Misuse of Channel Closing Semantics

Default severity: `SMELL`.
Pattern: sentinel values like `(>! ch :done)` followed by consumers checking `(= msg :done)`.
Fix: close the channel with `(close! ch)`. A take returns the remaining buffered values, then `nil` once the channel is closed and empty, so consumers loop with `when-let` or `when-some`.

#### Overengineering with `core.async`

Default severity: `SMELL`.
Pattern: channels used for one-shot results, as in a function that creates a channel only to put one HTTP response on it.
Fix: use the simplest mechanism: a plain function call or a callback, a `promise` or `future` on the JVM, or a promise library such as promesa in ClojureScript. Channels are for streams of events, fan-out, backpressure, and pipelines.
False positive: legitimate fan-out, batching, or backpressure scenarios.

### Namespace and Loading

#### Namespace Load Side Effects

Default severity: `SMELL` (`DEFECT` when the side effect mutates external state).
Pattern: `require` or `requiring-resolve` in a top-level form outside the `ns` form, as in `(def get-setting (requiring-resolve 'my-app.config/get-setting))`. The dependency is hidden from the `ns` form and from tools that read it. Other top-level effects, such as `(println ...)` or `(reset! global-atom ...)`, also run on every load (Iron Law CLJ-8 in the `clojure` skill).
Fix: declare dependencies in the `ns` form. Move other effects into a function called from `-main` or a system component.

#### Single-segment Namespace

Default severity: `SMELL`.
Pattern: `(ns digest)` instead of `(ns my-app.digest)`.
Why: collision risk and ecosystem convention violation.
Fix: nest under a project namespace.

#### Relying on Load-Time Side Effects

Default severity: `DEFECT`.
Pattern: a top-level `def` whose value is computed when the namespace loads from mutable global state or an expensive call, as in `(def config (calculate-heavy-config @global-atom))`.
Why: the value depends on what the running system held at load time, so reloading at the REPL or changing the load order changes the program.
Fix: compute the value in a function that callers invoke, or wrap it in `delay` when it should be computed once on first use.

#### Monolithic Namespace Split

Default severity: `SMELL`.
Pattern: multiple files using `(load "...")` and `(in-ns 'foo.bar)` to share one logical namespace.
Why: breaks static analysis, dependency resolution, and REPL reloading.
Fix: split into separate namespaces with clear `:require` relationships.

### Idioms and Style

#### Improper Emptiness Check

Default severity: `HINT`.
Pattern: `(when (not (empty? coll)) ...)` or `(when-not (empty? coll) ...)` to test that a collection has items.
Fix: use `(when (seq coll) ...)`. `seq` returns `nil` for empty collections. To test for emptiness, as in `(= 0 (count coll))`, use `(empty? coll)`.
clj-kondo linter: `:not-empty?`, on by default, reports `(not (empty? x))` when it can infer that `x` is a sequence.

#### Unnecessary `into`

Default severity: `HINT`.
Pattern: `(into [] coll)` or `(into #{} coll)` for simple type conversion, or `(into {} (map (fn [[k v]] [k (f v)]) m))` to update every value.
Fix: use `vec`, `set`, or `(update-vals m f)`. Apply `into` when adding to an existing collection or when transducing.

#### Verbose Checks

Default severity: `HINT`.
Pattern: `(= 0 x)`, `(> x 0)`, `(< x 0)`, `(+ x 1)`, `(- x 1)`.
Fix: `zero?`, `pos?`, `neg?`, `inc`, `dec`. The predicates require a number: `(zero? nil)` throws, and `(zero? 0.0)` is `true` while `(= 0 0.0)` is `false`, so confirm that `x` is always a number before rewriting.
clj-kondo linters: `:plus-one` and `:minus-one`, off by default, report `(+ x 1)` and `(- x 1)` when enabled.

#### Thread Ignorance

Default severity: `HINT`.
Pattern: deeply nested function calls or verbose intermediate `let` bindings where `->` or `->>` would clarify the data flow.
Fix: rewrite with the appropriate threading macro.
False positive: code where intermediate bindings carry meaningful names.

#### Unnecessary Laziness

Default severity: `HINT`.
Pattern: `(map f coll)` where the result is immediately realized via `(into [] ...)`, `(reduce conj [] ...)`, or `doall`.
Fix: use the eager `mapv`, `filterv`, or `reduce`. A vector is not a sequence: `conj` adds to its end, so `(conj (mapv inc [1 2]) 0)` is `[2 3 0]` while `(conj (doall (map inc [1 2])) 0)` is `(0 2 3)`. Check how callers use the result before changing its type.

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
Pattern: `(MyRecord. v1 v2)` -- Java interop constructor syntax for a `defrecord` or `deftype`.
Why: it bypasses the constructor functions that `defrecord` generates. In ClojureDart the interop form also needs the extra meta, extension map, and hash arguments that the constructor functions supply.
Fix: use `(->MyRecord v1 v2)` or `(map->MyRecord {:field-a v1 :field-b v2})`. Prefer `map->MyRecord` when a record has many fields, because a positional call silently assigns swapped values when the field order changes.

#### Marker Protocol

Default severity: `SMELL`.
Pattern: `(defprotocol Tagged)` with no methods, used only as a type identifier.
Fix: check the concrete type with `instance?`, or give the protocol the methods that callers need.
False positive: ClojureScript core defines marker protocols such as `ISequential` ("Marker interface indicating a persistent collection of sequential items"), so code that extends those protocols, or follows that convention for its own collection types, is not a finding.

#### Private Multimethods

Default severity: `SMELL`.
Pattern: `(defmulti ^:private name ...)`. A `defmethod` in another namespace then fails to compile because the var is not public. `^:private` on a `defmethod` has no effect.
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
Why: the `doall` docstring notes that it causes "the entire seq to reside in memory", which can cause memory spikes for large sequences.
Fix: use `run!` or `doseq` for side effects, and `reduce` or a transducer to consume a sequence without holding it. `mapv` or `vec` also hold the whole result, so use them only when the caller needs the realized collection.
False positive: realizing a sequence inside `with-open`, `binding`, or `try` before the scope closes (Iron Law CLJ-2 in the `clojure` skill), REPL utilities, one-off scripts, and small bounded sequences where eager realization is the explicit intent.

### Resources and I/O

#### Unmanaged Resource I/O

Default severity: `DEFECT`.
Pattern: a resource opened without cleanup, as in the JVM example `(let [r (clojure.java.io/reader path)] ...)` without `with-open`, or any closeable resource left without an explicit `.close`.
Why: file handles, sockets, and database connections leak.
Fix: on the JVM, use `with-open`, and realize any lazy result inside it, because `with-open` closes the resource before a lazy sequence is read:

```clojure
(with-open [r (clojure.java.io/reader path)]
  (doall (line-seq r)))
```

ClojureDart's `with-open` calls `.dispose` by default, and a `:dispose` directive after the binding, such as `:dispose .close`, names another cleanup method. ClojureScript has no `with-open`, so close the resource in `try`/`finally`. `clojure-jvm/references/errors-and-resources.md` in the `clojure-jvm` skill (clojure-jvm-skills) and `clojuredart/references/errors-and-resources.md` in the `clojuredart` skill (clojuredart-skills) cover each host's cleanup.

### Data

#### Map With Nil Values

Default severity: `SMELL`.
Pattern: `{:a 1 :b nil}` returned from a function or stored in state.
Why: a lookup returns `nil` both for an absent key and for a key bound to `nil`, so callers that only look up the key cannot tell the cases apart, and `(merge {:a 1} {:a nil})` replaces a value with `nil`.
Fix: omit `nil`-valued keys (`cond->`, `dissoc`, or filter the map).
False positive: maps where `nil` is a meaningful value distinct from "absent", checked with `contains?` or `find` (rare but legitimate).

#### Case with Non-Literal Test Values

Default severity: `DEFECT`.
Pattern: `(case x sym-bound-to-1 :one ...)` where `sym-bound-to-1` is a runtime value.
Why: `case` matches against the symbol literally, not its value, leading to silent misses.
Fix: use `condp =` or a `cond` with explicit equality.
clj-kondo linter: `:case-symbol-test`, off by default. The overlay turns it on at `:info`, so Stage 1 reports every symbol test constant, including each symbol in a grouped list such as `(a b)`, without failing a lint run. A symbol constant is correct when the code matches symbols as data, as in `(case op + :plus - :minus)` over operator symbols, so decide each finding from the code and never auto-fix it. A form that matches symbols deliberately can opt out with `#_{:clj-kondo/ignore [:case-symbol-test]}` before the `case`.

### React Hooks

#### Refs in Dependency Vector

Default severity: `DEFECT` (only in React hook dependency vectors).
Pattern: placing a mutable reference (an atom or ratom, a `use-state` object, or a `use-ref` object) in the dependency vector of an effect hook, as in `(use-effect (fn [] ...) [my-ratom])` with `reagent.hooks` or rumext.
Why: the hook compares the reference, not its value, so the effect never re-runs when the reference is stable or re-runs when the framework replaces it.
Fix: put the dereferenced value (`@my-ratom`) in the dependency vector.
False positive: the smell applies only to hook dependency vectors. Skip plain Clojure, Reagent components that do not use hooks, and re-frame subscriptions.
