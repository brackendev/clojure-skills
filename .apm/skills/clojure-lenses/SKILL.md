---
name: clojure-lenses
description: >-
  Translate code-lenses design philosophies into idiomatic Clojure. Auto-triggers
  when working in Clojure alongside code-lenses skills. Covers the four default
  code-lenses philosophies (grug, Honest Code, Tidy First, Parse Don't Validate)
  and the two opt-in philosophies (APOSD, Legacy Code) that activate when their
  lens is added with `+aposd` or `+legacy-code` or invoked directly. Prevents
  non-idiomatic translations of language-agnostic design advice.
user-invocable: false
---

# Code Lenses for Clojure

When the [code-lenses](https://github.com/brackendev/code-lenses) design philosophy skills are active alongside Clojure code, use these translations to prevent non-idiomatic advice. Each section maps a code-lenses philosophy to Clojure-native tools and patterns.

The default code-lenses review set is `grug`, `honest-code`, `tidy-first`, and `parse-dont-validate`. `aposd` and `legacy-code` are opt-in (added with `+aposd` or `+legacy-code`, or invoked directly via `/aposd-review` and `/legacy-code-review`). The sections below apply when the corresponding lens is active; APOSD and Legacy Code translations are present so the lens can use them when explicitly invoked, but they are not part of the default trigger set.

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
- **Schemas at boundaries.** On the JVM and in ClojureScript, use `clojure.spec` or Malli to define and enforce interface contracts at system entry points. ClojureDart has neither, so write the boundary as a parsing function that returns a domain map or throws `ex-info`. This pulls validation complexity downward: callers pass plain data, the boundary namespace handles conformance.
- **A system map for lifecycle.** System lifecycle (starting databases, HTTP servers, caches) is hidden complexity. Push it into a system map. On the JVM and in ClojureScript, Component, Integrant, or Mount can manage it. None of them supports ClojureDart, so keep the same shape there with explicit start and stop functions. Business logic namespaces should not know how dependencies start or stop.

## Tidy First

The 15 tidyings apply to Clojure with these translations:

- **Guard clauses** means flattening nested `if`/`when`/`let` with `cond`, `if-let`, `when-let`, `some->`, and `some->>`. Clojure has no early return; flatten with expression-oriented conditional forms instead.
- **Explaining variables** means extracting complex expressions into named `let` bindings that reveal intent.
- **Extract helper** means pulling a block from a `let` body or threading pipeline into a named `defn-` or `letfn` function.
- **Reading order** is nuanced in Clojure. Clojure files compile top-down, so helpers must be defined before callers unless `declare` is used. Many Clojure codebases prefer helpers-first order naturally. Do not force `declare` or reorder files for top-down reading if it fights the existing convention.
- **Move declaration and initialization together** means placing `let` bindings close to their first use rather than at the top of a large `let` block. Split large `let` blocks into smaller scopes when bindings serve different concerns.
- **Chunk statements** is limited in Clojure, because the `clojure` skill's Formatting rules allow a blank line inside a definition only between `let` or `cond` pairs that do not fit on one line. Chunk other related work by splitting a large `let` into smaller scopes or by extracting a group of pipeline stages into a named helper.
- **Normalize symmetries** means making similar `cond` branches, `case` clauses, or multimethod implementations follow identical structure so differences stand out.
- **New interface, old implementation** means writing a new function with the desired signature that delegates to the existing function. Useful for wrapping host interop.
- Threading macros (`->`, `->>`, `as->`) are a tidying tool: they flatten nested calls into a readable pipeline. Use them to replace deeply nested function calls.
- REPL-guided refactoring is the natural workflow. Evaluate forms, test transformations interactively, and apply structural changes with confidence.
- `clj-kondo` catches dead code, unused bindings, and other structural issues that inform which tidyings to apply.

## Parse, Don't Validate

The principle applies fully in Clojure, but the mechanism differs from statically typed languages. Clojure makes illegal states harder to construct and easier to detect, not compile-time impossible.

- **The parse layer.** On the JVM and in ClojureScript, `clojure.spec` and Malli are the parse layer: use `s/conform` or Malli coercion at the boundary to transform raw input (JSON, EDN, query parameters, environment variables) into domain maps. In ClojureDart, which has neither, a smart constructor function is the parse layer. Downstream functions receive conformed data and do not re-validate.
- **Smart constructor functions.** Write a `make-order` or `parse-email` function that validates input and returns a normalized domain map or throws `ex-info` with structured error data. Callers that hold the result know it satisfies the invariants.
- **Namespaced keys as domain markers.** Use `:order/id`, `:order/total`, `:user/email` instead of bare `:id`, `:total`, `:email`. Namespaced keys signal that the data has been parsed and belongs to a specific domain concept.
- **Tagged maps for sum types.** Use a `:type` or `:kind` key to distinguish variants (e.g., `{:type :loading}`, `{:type :loaded, :data [...]}`, `{:type :error, :message "..."}`) instead of multiple boolean/nullable fields. Dispatch on the tag with `case`, `cond`, or multimethods.
- **Closed maps with Malli.** On the JVM and in ClojureScript, use Malli's `:map` schema with `:closed` to reject unexpected keys at the boundary. This catches typos and stale fields early. A ClojureDart smart constructor can reject keys outside an allowed set the same way.
- **Do not simulate branded types.** Wrapping strings in records or deftypes to emulate TypeScript-style branded types is non-idiomatic in Clojure. Use smart constructors and namespaced keys instead, with spec or Malli predicates where the host has them. The discipline is convention-enforced, not compiler-enforced.
- **Collect all parse failures.** Use `s/explain-data` or Malli's `m/explain` to report all validation errors at once, not one at a time. A smart constructor can do the same by putting every failure in its `ex-info` data.

## Honest Code

Cite principles by number from the Honest Code Principles file that the code-lenses `honest-code` skill ships (`honest-code/references/honest-code-principles.md`). Most principles apply to Clojure as written. These notes translate the ones whose wording assumes another language. Where Clojure cannot meet a principle as written, report a departure with its reason, not conformance.

- **P02 (Pure Functions Over Methods):** Already the default. Clojure maps, vectors, and sets are plain data with no behavior attached. Upstream's test is whether a value survives `json.dumps()`. In Clojure the equivalent test is whether the value prints and reads back as EDN, which carries keywords, sets, and tagged literals that JSON lacks, so do not force JSON-shaped constraints on Clojure data.
- **P01 (Lookup Polymorphism) and P16 (Dispatch Tables Close Open Input):** A map from keys to functions, a multimethod, or protocol dispatch on the first argument is the dispatch table. When a catalog or registry decides behavior, keep the relationships in the catalog entries and resolve them with one pure function per question (`clojure/references/data-driven-design.md` in the `clojure` skill). P16 asks for an unknown key to raise. A `defmulti` whose `:default` method returns an answer, `(get handlers k fallback)`, and `(handlers k fallback)` all file an unhandled key under another key's answer. Let the missing-method or missing-key failure raise, or return the miss as a named case that the caller must handle.
- **P04 (Composition Over Inheritance):** Threading macros (`->`, `->>`, `as->`), `comp`, transducers, and `sequence` are the Clojure pipeline. Ring middleware stacks on the JVM are flat composition by convention. An orchestrator function that calls another orchestrator still breaks P04.
- **P08 (Typed Exceptions at the Boundary) and P24 (A Fault Travels in the Return Value):** Raise `ex-info` with data at the source, and let the boundary (a Ring handler, a CLI entry point, or a job runner) catch it and dispatch on a key in `ex-data`, such as `:type`. Do not catch inside business logic. Retries belong to the job or queue infrastructure, such as a system lifecycle tool (Component or Integrant) or a message queue with a retry policy, not to the function. Clojure functions have no checked return types, so the type-system guarantee that P24 describes is unavailable. Where a fault is expected, return a value that names it (`{:status :not-found}`) and declare that shape in the docstring or a Malli function schema. Report the missing type guarantee as a departure from P24.
- **P10 (Pure Function Assertions Over Mocks):** `(is (= (f input) expected))` is the whole test, and `are` or `doseq` over data-driven cases keeps tests flat in `clojure.test`, `cljs.test`, and `cljd.test`. Upstream's `honest-test` refuses runtime rebinding, so `with-redefs` counts as a mock under this lens. A test that needs it, or complex fixtures, marks a hidden dependency to extract. The Legacy Code seams below are temporary scaffolding for code without tests, not the end state.
- **P11 (Type Declarations Over Imperative Validation) and P14 (Trust the Contract in the Interior):** Spec and Malli schemas, on the hosts that have them, are the declarations. Prefer them to hand-written validation loops, validate once at the boundary, and do not re-check the same constraint inside.
- **P15 (No Implicit Defaults):** `:or` in a function's argument destructuring is a defaulted parameter. Resolve absence once at the boundary, where the decision is visible and tested, instead of letting each function absorb a missing key.
- **State (P02, P12, P17):** Upstream does not discuss reference types. This package reads atoms, and refs and agents on the JVM, as explicit state: each is named and changed only through a defined interface, unlike the method-held state that P02 rejects. Keep the function passed to `swap!` pure (Iron Law CLJ-3), so each change is a pure function applied atomically. `swap!` and `compare-and-set!` are the atomic test-and-set that P17 asks for, and a `deref` followed by a `reset!` is the check-then-act that P17 forbids. Open resources within a scope, with `with-open` where the host has it or in a lifecycle component, not in a top-level `def` (P12).
- **Browser principles (P05, P06, P22):** These apply when the work renders a browser interface. P05 makes server-rendered HTML the only copy of client state. Reagent, re-frame, and Fulcro keep a client-side copy, so a project built on them departs from P05. Report that as a departure with its reason, such as an offline or highly interactive interface, not as conformance. `clojurescript-lenses`, `fulcro-lenses`, and `electric-lenses` translate these principles for their frameworks.

## Legacy Code

The book's techniques are OO-flavored. Translate to Clojure-native seams and strategies:

- **Seams in Clojure:** Vars (rebound with `with-redefs` in tests on the JVM and in ClojureScript, because ClojureDart has no `with-redefs`), higher-order functions (pass a function instead of hardcoding a call), protocols at system boundaries (swap implementations for test doubles), and system maps (Component, Integrant, and Mount provide dependency injection on the JVM and in ClojureScript). Prefer extracting pure functions over relying on `with-redefs`.
- **Extract Interface** translates to extracting a protocol, but only when multiple implementations are needed at a boundary. For most cases, extracting a function parameter (higher-order function) is simpler and sufficient.
- **Parameterize Constructor** translates to passing a deps map or system key. Functions that construct side-effecting resources should receive their dependencies as arguments or through a system map, not reach for globals.
- **Sprout method/class** translates to sprouting a new function or namespace. Add new behavior in a new `defn` called from the existing code, rather than modifying an untested function directly.
- **Wrap method/class** translates to a wrapper function or namespace. Write a new function that calls the original and adds pre/post behavior. Useful for wrapping host interop or legacy namespaces.
- **Scratch refactoring** maps naturally to REPL-driven exploration. Evaluate forms, trace execution paths, and `macroexpand` macro-heavy code to understand structure before making targeted changes.
- **Effect sketching** means tracing which vars and namespaces a change touches. Use `clj-kondo` or editor tooling to map the dependency graph. In Clojure, the narrowest test point is often a pure function that can be called directly in the REPL.
- **Hard dependencies in Clojure** include direct host interop calls, `def` state at the namespace level, side effects in function bodies, and `require` of concrete implementations where a protocol or function parameter would allow substitution.
- **Characterization tests** in Clojure use `clojure.test`, `cljs.test`, or `cljd.test`. Capture existing behavior with `(is (= (f known-input) observed-output))` before making changes. The REPL is the fastest way to discover what existing functions return.
