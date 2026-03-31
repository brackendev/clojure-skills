---
name: clojure-lenses
description: >-
  Translate code-lenses design philosophies (grug, APOSD, Tidy First, Parse Don't
  Validate, Honest Code, Legacy Code) to idiomatic Clojure. Auto-triggers when
  working in Clojure alongside code-lenses skills. Prevents non-idiomatic
  translations of language-agnostic design advice.
user-invocable: false
---

# Code Lenses for Clojure

When the [code-lenses](https://github.com/brackendev/code-lenses) design philosophy skills are active alongside Clojure code, use these translations to prevent non-idiomatic advice. Each section maps a code-lenses philosophy to Clojure-native tools and patterns.

## Grug Brain

Grug philosophy aligns naturally with Clojure. Translate grug instincts to Clojure-native tools:

- Default building blocks are maps, vectors, and sets with namespaced keys. Not records, not deftypes, not protocols. Reach for those only when a concrete need appears.
- "Guard clauses and early returns" means flattening control flow with `cond`, `when`, `if-let`, `some->`, and `some->>`. Clojure has no `return` statement; use expression-oriented flow instead.
- "Composition over inheritance" means `comp`, threading macros (`->`, `->>`), transducers, and plain function calls. Not protocols wrapping protocols.
- REPL-driven development is grug's prototype instinct made real. Spike in the REPL before introducing abstractions.
- Protocols and multimethods are powerful, but they are abstraction. Use them when open polymorphism is required (multiple independent implementations). A `cond` or map lookup on a keyword is simpler when the set of cases is closed and small.
- Transducers, the sequence abstraction, and reducers are not complexity demons. They are built-in composition tools. Use them when they make code shorter and clearer.
- Macros are a last-resort tool for syntax, not a primary abstraction mechanism. Prefer functions.

## A Philosophy of Software Design

APOSD maps well to Clojure. Translate module-level thinking to Clojure-native structures:

- **Namespaces are modules.** A namespace with a small public API (`defn`) and private helpers (`defn-`) is a deep module. Evaluate depth by the ratio of public functions to total implementation.
- **Seqs and transducers are deep modules.** The sequence abstraction is a textbook example: simple interface (`first`, `rest`, `cons`), powerful implementation across all collection types. Transducers add composable transformation with a narrow interface (`xform`). Recognize and preserve these.
- **Information hiding means hiding policy, not data.** Clojure idiomatically passes plain maps as data. Hide interop details, caching, retry logic, connection management, and lifecycle concerns behind namespace boundaries. Do not wrap maps in record facades for the sake of encapsulation.
- **`defn-` for privacy.** Use private functions to hide implementation decisions. Callers should depend on the public API, not internal helpers.
- **`ex-info` and `ex-data` for rich errors.** When errors cannot be defined out of existence, use `ex-info` to attach structured context. Handle at the boundary with `ex-data` destructuring. This is Clojure's equivalent of exception aggregation.
- **`spec`/Malli at boundaries.** Use `clojure.spec` or Malli to define and enforce interface contracts at system entry points. This pulls validation complexity downward: callers pass plain data, the boundary namespace handles conformance.
- **Component/Integrant/Mount for lifecycle.** System lifecycle (starting databases, HTTP servers, caches) is hidden complexity. Push it into a system map managed by Component, Integrant, or Mount. Business logic namespaces should not know how dependencies start or stop.

## Tidy First

The 15 tidyings apply to Clojure with these translations:

- **Guard clauses** means flattening nested `if`/`when`/`let` with `cond`, `if-let`, `when-let`, `some->`, and `some->>`. Clojure has no early return; flatten with expression-oriented conditional forms instead.
- **Explaining variables** means extracting complex expressions into named `let` bindings that reveal intent.
- **Extract helper** means pulling a block from a `let` body or threading pipeline into a named `defn-` or `letfn` function.
- **Reading order** is nuanced in Clojure. Clojure files compile top-down, so helpers must be defined before callers unless `declare` is used. Many Clojure codebases prefer helpers-first order naturally. Do not force `declare` or reorder files for top-down reading if it fights the existing convention.
- **Move declaration and initialization together** means placing `let` bindings close to their first use rather than at the top of a large `let` block. Split large `let` blocks into smaller scopes when bindings serve different concerns.
- **Chunk statements** means adding blank lines between groups of related `let` bindings or between pipeline stages.
- **Normalize symmetries** means making similar `cond` branches, `case` clauses, or multimethod implementations follow identical structure so differences stand out.
- **New interface, old implementation** means writing a new function with the desired signature that delegates to the existing function. Useful for wrapping Java interop.
- Threading macros (`->`, `->>`, `as->`) are a tidying tool: they flatten nested calls into a readable pipeline. Use them to replace deeply nested function calls.
- REPL-guided refactoring is the natural workflow. Evaluate forms, test transformations interactively, and apply structural changes with confidence.
- `clj-kondo` catches dead code, unused bindings, and other structural issues that inform which tidyings to apply.

## Parse, Don't Validate

The principle applies fully in Clojure, but the mechanism differs from statically typed languages. Clojure makes illegal states harder to construct and easier to detect, not compile-time impossible.

- **`clojure.spec` and Malli are the parse layer.** Use `s/conform` or Malli coercion at the boundary to transform raw input (JSON, EDN, query parameters, environment variables) into domain maps. Downstream functions receive conformed data and do not re-validate.
- **Smart constructor functions.** Write a `make-order` or `parse-email` function that validates input and returns a normalized domain map or throws `ex-info` with structured error data. Callers that hold the result know it satisfies the invariants.
- **Namespaced keys as domain markers.** Use `:order/id`, `:order/total`, `:user/email` instead of bare `:id`, `:total`, `:email`. Namespaced keys signal that the data has been parsed and belongs to a specific domain concept.
- **Tagged maps for sum types.** Use a `:type` or `:kind` key to distinguish variants (e.g., `{:type :loading}`, `{:type :loaded, :data [...]}`, `{:type :error, :message "..."}`) instead of multiple boolean/nullable fields. Dispatch on the tag with `case`, `cond`, or multimethods.
- **Closed maps with Malli.** Use Malli's `:map` schema with `:closed` to reject unexpected keys at the boundary. This catches typos and stale fields early.
- **Do not simulate branded types.** Wrapping strings in records or deftypes to emulate TypeScript-style branded types is non-idiomatic in Clojure. Use spec/Malli predicates, smart constructors, and namespaced keys instead. The discipline is convention-enforced, not compiler-enforced.
- **Collect all parse failures.** Use `s/explain-data` or Malli's `m/explain` to report all validation errors at once, not one at a time.

## Honest Code

Most Honest Code constructs are native Clojure. Translate the remaining constructs with these adjustments:

- **Construct 1 (Data Is Data):** Already the default. Clojure maps, vectors, and sets are the honest data structures. The serialization test is EDN-printable, not JSON-serializable. EDN supports more types than JSON (keywords, sets, tagged literals). Do not force JSON-shaped constraints on Clojure data.
- **Construct 2 (Input In, Output Out):** Native. Pure functions over immutable data are idiomatic Clojure. Polymorphism via multimethods or protocol dispatch on the first argument is honest when the dispatch is data-driven.
- **Construct 3 (One Source of Truth):** The principle applies, but the web-specific guidance (HTMX, server-rendered HTML) is one option, not the only honest one. Reagent, re-frame, and Fulcro are valid Clojure/ClojureScript choices when the database alone cannot serve as the single source for UI state. The key test is: does one authoritative source own each piece of state?
- **Construct 5 (Compose Flat, Never Deep):** Threading macros (`->`, `->>`, `as->`), `comp`, transducers, and `sequence` are the Clojure composition tools. Middleware stacks (Ring) are flat composition by convention, not deep hierarchies.
- **Construct 6 (Let It Crash):** Use `ex-info`/`ex-data` instead of typed exceptions. Raise with structured data at the source, catch at the boundary with `ex-data` destructuring. For background processes, supervision means a system lifecycle tool (Component/Integrant) or a message queue with retry policy. Clojure on the JVM does not have Erlang-style supervisors, so "let it crash" means "fail loudly and let the outer boundary decide," not "crash the process."
- **Construct 8 (Boring Tests):** `(is (= (f input) expected))` is the boring test. `clojure.test` with data-driven test cases (via `are` or `doseq`) keeps tests flat and repetitive. If tests need `with-redefs` or complex fixtures, the design is the bug.
- **Construct 10 (Declare What, Not How):** Spec/Malli schemas are declarations. `defmulti` dispatch is a declaration. Prefer these over imperative validation loops.
- **State is not dishonest when explicit.** Atoms, refs, and agents are honest state management: they make mutability visible, controlled, and coordinated. Rejecting all state is not idiomatic Clojure. The honest test is whether state is isolated, named, and managed through a defined interface (`swap!`, `send`, `dosync`), not hidden in mutable fields.

## Legacy Code

The book's techniques are OO-flavored. Translate to Clojure-native seams and strategies:

- **Seams in Clojure:** Vars (rebound with `with-redefs` in tests), higher-order functions (pass a function instead of hardcoding a call), protocols at system boundaries (swap implementations for test doubles), and system maps (Component/Integrant/Mount provide dependency injection). Prefer extracting pure functions over relying on `with-redefs`.
- **Extract Interface** translates to extracting a protocol, but only when multiple implementations are needed at a boundary. For most cases, extracting a function parameter (higher-order function) is simpler and sufficient.
- **Parameterize Constructor** translates to passing a deps map or system key. Functions that construct side-effecting resources should receive their dependencies as arguments or through a system map, not reach for globals.
- **Sprout method/class** translates to sprouting a new function or namespace. Add new behavior in a new `defn` called from the existing code, rather than modifying an untested function directly.
- **Wrap method/class** translates to a wrapper function or namespace. Write a new function that calls the original and adds pre/post behavior. Useful for wrapping Java interop or legacy namespaces.
- **Scratch refactoring** maps naturally to REPL-driven exploration. Evaluate forms, trace execution paths, and `macroexpand` macro-heavy code to understand structure before making targeted changes.
- **Effect sketching** means tracing which vars and namespaces a change touches. Use `clj-kondo` or editor tooling to map the dependency graph. In Clojure, the narrowest test point is often a pure function that can be called directly in the REPL.
- **Hard dependencies in Clojure** include direct Java interop calls, `def` state at the namespace level, side effects in function bodies, and `require` of concrete implementations where a protocol or function parameter would allow substitution.
- **Characterization tests** in Clojure use `clojure.test`. Capture existing behavior with `(is (= (f known-input) observed-output))` before making changes. The REPL is the fastest way to discover what existing functions return.
