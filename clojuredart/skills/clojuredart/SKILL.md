---
name: clojuredart
description: >-
  ClojureDart expertise for working with .cljd files, deps.edn with ClojureDart
  dependencies, and Flutter projects with cljd-out/ directories. Provides syntax,
  Dart interop patterns, project structure, and compilation knowledge.
user-invocable: false
---

# ClojureDart

ClojureDart is a Clojure dialect that compiles to Dart, enabling Clojure development within Flutter projects. Dart is more strongly typed and less dynamic than Java. This lack of dynamism is the price for excellent tree-shaking and fast startups.

## When to Apply

Apply this knowledge when:

- Working with `.cljd` files
- A `deps.edn` contains `tensegritics/clojuredart` as a dependency
- A Flutter project contains a `lib/cljd-out/` directory
- The user mentions ClojureDart or cljd

## CLI

```bash
clj -M:cljd init       # Initialize a ClojureDart project (creates Flutter structure)
clj -M:cljd flutter    # Compile, watch, and hot reload (dev mode); press RETURN to force restart
clj -M:cljd compile    # AOT compilation (for deployment)
clj -M:cljd clean      # Clean build artifacts
clj -M:cljd upgrade    # Update to latest ClojureDart
clj -M:cljd test       # Run tests
```

## Dart Interop Syntax

### Property Access

```clojure
;; Read: (.-prop obj)
(.-length my-list)
(.-isNotEmpty my-string)

;; Set: (.-prop! obj value) or (set! (.-prop obj) value)
(.-title! app-bar "New Title")
(set! (.-text controller) "hello")

;; Chained static properties
m/Colors.purple.shade900
;; Can terminate with a method call
(m/Colors.purple.shade900.withAlpha 128)
```

### Method Calls

```clojure
;; (.method object args...)
(.contains url "example")
(.format formatter date)
(.replaceAll s ".00" "")

;; Named parameters: .paramName value
(.firstWhere list predicate .orElse fallback-fn)
(m/Text "Hello world" .maxLines 2 .softWrap true .overflow m/TextOverflow.fade)
```

### Constructors

ClojureDart constructors do not use trailing dot or `new`. Classes in function position call the default constructor:

```clojure
;; Default constructor: class in function position
(StringBuffer "hello")
(m/TextStyle .color m/Colors.red .fontSize 32.0)
(m/ThemeData .primarySwatch m.Colors/pink)

;; Named constructor: called like static methods
(List/empty .growable true)
(DateTime/now)
(tz/TZDateTime.from date-time city-tz)
```

### Static Methods and Properties

```clojure
;; Static method
(DateTime/parse "2024-01-01")
(tz/getLocation "America/New_York")

;; Static properties (including enums)
m/TextAlign.left
m/Colors.red
m/TextOverflow.fade
```

### Extension Methods

Extension methods require explicit qualification with the extension type:

```clojure
;; Dart: now.copyWith(year: now.year - 1)
;; ClojureDart:
(let [now (DateTime/now)]
  (-> now dart:core/DateTimeCopyWith (.copyWith .year (dec (.-year now)))))
```

## Types

### Type Hints

```clojure
;; Parameter type hints
(defn my-fn [^String s ^int n] ...)

;; Type hints on intermediate values
(.replaceAll ^String r pattern replacement)
```

### Nullability

`^String` means the value cannot be nil. Use `^String?` to allow nil:

```clojure
(defn greet [^String name]    ...)   ;; name is never nil
(defn greet [^String? name]   ...)   ;; name can be nil
```

### Parametrized Types

Dart generics exist at runtime. Use `#/(Type Params)` for type hints and constructors:

```clojure
;; As type hints
^#/(List String) items            ;; List<String>
^#/(Map String int) counts        ;; Map<String, int>

;; As parametrized constructors
(#/(m/GlobalKey m/FormState))                          ;; GlobalKey<FormState>()
(#/(m/MaterialPageRoute Object) .builder (f/build w))  ;; MaterialPageRoute<Object>(builder: ...)
```

### Dynamic Warnings

Dynamic warnings are similar to reflection warnings in Clojure but more serious. They occur when the compiler cannot infer the type of an object for a method call. Unlike JVM Clojure, Dart lacks reflective features, so dynamic calls:

- Are slower
- Can fail at runtime because arguments are not properly cast

Fix dynamic warnings immediately, starting with the first one (they cascade). Add type hints to resolve them.

## Visibility and Export

```clojure
(defn ^:export my-function [args] ...)  ;; Dart-visible
(defn- helper-function [args] ...)      ;; Namespace-private
```

## Async

Use `await` for Dart async operations:

```clojure
(let [result (await (some-async-call))]
  (process result))
```

## Object Destructuring

### :flds for Named Fields

`:flds` destructures Dart object properties, similar to `:keys` for maps:

```clojure
(let [{:flds [height width]} size]
  (* height width))

;; Equivalent to:
(let [height (.-height size)
      width  (.-width size)]
  (* height width))
```

### .-property Keys in Destructuring

Use `.-property` as destructuring keys to extract Dart properties inline. Common in event handler parameters:

```clojure
;; Extract localPosition from DragStartDetails
(fn [{local-pos .-localPosition :as ^g/DragStartDetails details}] ...)

;; Nested: extract dx/dy from localPosition
(fn [^m/DragUpdateDetails {{:flds [dx dy]} .-localPosition}] ...)

;; Deep chain through :get
:get {{{:flds [displayLarge]} .-textTheme} m/Theme}
```

## Function Parameters

### Named Parameters

Use `.paramName` for Dart named parameters in function definitions:

```clojure
[a b c .d .e]         ;; 3 positional, 2 optional named (d, e)
[.a 42 .e]            ;; 2 named: a defaults to 42, e is required
```

### Optional Positional Parameters

Use `...` to mark the boundary between fixed and optional positional parameters:

```clojure
[a b c ... d e]       ;; 3 fixed positional, 2 optional positional
[... a 42 b]          ;; 2 optional positional: a defaults to 42
```

## Namespace Conventions

```clojure
(ns my-app.feature
  (:require
   ;; Dart package imports: string + :as
   ["package:flutter/material.dart" :as m]
   ["package:intl/intl.dart" :as intl]
   [cljd.flutter :as f]

   ;; App-internal Dart imports
   ["package:my_app/constants.dart" :as constants]

   ;; Other .cljd files: Clojure-style requires
   my-app.extensions.string))
```

Dart package names in require strings use underscores. ClojureDart namespace segments use hyphens. File `src/my_app/feature.cljd` maps to namespace `my-app.feature`.

## Project Structure

```
my-project/
  deps.edn              # Clojure deps with ClojureDart config
  .cljfmt.edn           # cljfmt formatting rules
  src/
    my_app/
      main.cljd          # Entry point
      feature.cljd       # Feature modules
  lib/
    cljd-out/            # Generated Dart (NEVER edit)
    main.dart            # Dart entry point
  pubspec.yaml           # Flutter/Dart dependencies
  analysis_options.yaml  # Dart analysis config
```

## deps.edn Configuration

```clojure
{:paths     ["src"]
 :deps      {org.clojure/clojure {:mvn/version "1.11.0"}
             tensegritics/clojuredart
             {:git/url "https://github.com/tensegritics/ClojureDart.git"
              :sha     "<commit-sha>"}}
 :aliases   {:cljd {:main-opts ["-m" "cljd.build"]}
             :cljfmt {:extra-deps {dev.weavejester/cljfmt {:mvn/version "0.13.0"}}
                      :main-opts ["-m" "cljfmt.main"]}}
 :cljd/opts {:kind :flutter
             :main my-app.main}}
```

## Consts

ClojureDart maximally infers `const` expressions (Dart compile-time constants). In rare cases where you need a unique instance (like a sentinel), tag with `^:unique`:

```clojure
^:unique (Object)   ;; Guaranteed unique instance, not deduplicated
```

## Creating Classes

`reify`, `deftype`, and `defrecord` support Dart class features:

### Extending Classes

```clojure
(deftype MyWidget []
  :extends (m/StatelessWidget)
  (build [this context]
    (m/Text "hello")))
```

### Mixins

Tag mixin types with `^:mixin`:

```clojure
(deftype MyClass []
  :extends SomeBase
  ^:mixin SomeMixin
  ...)
```

### Operator Overloading

Use strings for operators that are not valid Clojure symbols:

```clojure
(. list "[]=" i 42)   ;; list[i] = 42
```

For operators that are valid Clojure symbols, use dot-method syntax:

```clojure
(.- a b)    ;; a - b (subtraction, e.g. Offset subtraction)
(.+ a b)    ;; a + b (addition)
```

Note: `(.- obj)` with one argument is property access. `(.- a b)` with two arguments is the `-` operator.

### Getters and Setters

Defined as methods in `reify`/`deftype`/`defrecord`. A getter takes `[this]`, a setter takes `[this v]`. New properties need `^:getter` or `^:setter` tags:

```clojure
(deftype MyType [^:mut _value]
  (^:getter value [this] _value)
  (^:setter value [this v] (set! _value v)))
```

## cljd.flutter

`cljd.flutter` is a utility library that removes Flutter boilerplate. Always require it:

```clojure
(ns my-app.main
  (:require
   [cljd.flutter :as f]
   ["package:flutter/material.dart" :as m]))
```

### f/run

Starting point for a Flutter application. Called from `main`:

```clojure
(defn main []
  (f/run
    (m/MaterialApp .title "My App")
    .home
    (m/Scaffold .appBar (m/AppBar .title (m/Text "Hello")))
    .body
    m/Center
    (m/Text "Let's get coding!")))
```

### f/build

Creates a builder callback (a function that returns a widget). Used for Flutter APIs that expect builder functions like `itemBuilder`, `builder`, etc.:

```clojure
;; Simple builder (no params) -- wraps a widget as a builder function
(#/(m/MaterialPageRoute Object) .builder (f/build second-route))

;; Builder with index parameter (for itemBuilder)
(m/ListView.builder
  .itemCount 25
  .itemBuilder (f/build [i] (fake-item (odd? i))))

;; Builder with directives
(f/build
  :get [m/Navigator]
  (m/AlertDialog .content (m/Text "hello")))
```

### f/widget

Evaluates to a Widget. Its body is interleaved expressions and directives. Expressions are `.child`-threaded by default:

```clojure
;; Expressions thread through .child
(f/widget
  m/Center
  (m/Text "hello"))
;; Equivalent to: (m/Center .child (m/Text "hello"))

;; Use dotted symbols to thread through other named params
(f/widget
  m/MaterialApp
  .home
  m/Scaffold
  .body
  m/Center
  (m/Text "hello"))
```

### Directives

Directives are keywords followed by a form, used inside `f/widget` and `f/run`:

#### :let

```clojure
(f/widget
  :let [name "World"]
  (m/Text (str "Hello " name)))
```

#### :watch (reactive state)

Watches atoms, Futures, Streams, Listenables, and other watchables. Rebuilds when values change:

```clojure
(f/widget
  :watch [v an-atom]
  (m/Text (str v)))
```

Options:

```clojure
;; :default -- initial value before async watchable produces
:watch [v (Future/delayed (Duration .seconds 3) (fn [] "Done"))
        :default "Loading..."]

;; :as -- name the watchable for mutation
:watch [n (atom 0) :as counter]
;; Then use: (swap! counter inc)

;; :dispose -- custom cleanup (defaults to nil, unlike :managed which defaults to .dispose)
:watch [v some-resource :as r :dispose .close]

;; :refresh-on -- control when watchable is recomputed (nil = never refresh)
:watch [data (atom initial-value) :as state :refresh-on nil]

;; :> -- extract value from notifier-style watchables
:watch [v some-notifier :> .-value]
```

Deduplication: `:watch` only triggers rebuild when bound values change (by `=`). Destructuring helps: `{:keys [name]} big-atom` only rebuilds when `name` changes.

#### :managed (lifecycle management)

Automatic lifecycle for controllers and disposable objects. Calls `.dispose` by default:

```clojure
(f/widget
  :managed [controller (m/TextEditingController)]
  (m/TextField .controller controller))
```

#### :bind and :get (inherited bindings)

Dynamic binding along the widget tree (inherited widgets):

```clojure
;; Establish binding
(f/widget
  :bind {:counters (atom {:left 0 :right 0})}
  child-widget)

;; Retrieve binding -- vector form (simple)
(f/widget
  :get [:counters]
  :watch [n counters]
  (m/Text (str n)))

;; Retrieve Flutter framework objects
(f/widget
  :get [m/Navigator]
  ;; navigator is bound (auto kebab-cased from Navigator)
  (.push navigator some-route))

;; Map form -- combines framework objects, :value-of, and destructuring
(f/widget
  :get {{{:flds [displayLarge]} .-textTheme} m/Theme
        :value-of [:counters]}
  :watch [{n :left} counters]
  (m/Text (str n) .style displayLarge))

;; Nested destructuring with property access
(f/widget
  :get {{{:flds [secondary onSecondary]} .-colorScheme} m/Theme}
  (m/Material .color secondary))
```

#### :context

Binds a `BuildContext` instance:

```clojure
(f/widget
  :context ctx
  (m/showDialog .context ctx ...))
```

#### :bg-watcher (background side effects)

Runs side effects when a watchable changes without triggering widget rebuild:

```clojure
(f/widget
  :managed [animator (m/AnimationController .vsync vsync)]
  :watch [{:keys [alignment animation]} state]
  :bg-watcher ([animated-value animation]
               (swap! state assoc :alignment animated-value))
  ...)
```

#### :vsync

Binds a `TickerProvider` for animations:

```clojure
(f/widget
  :vsync clock
  :managed [controller (m/AnimationController .vsync clock .duration (Duration .seconds 1))]
  ...)
```

#### :key

Wraps its value in a `ValueKey` for identifying sibling widgets:

```clojure
(f/widget
  :key item-id
  (m/ListTile .title (m/Text item-name)))
```

#### :when

Conditionally shows the rest of the widget:

```clojure
(f/widget
  :when show?
  (m/Text "Visible"))
```

#### :height, :width, :color

Shorthand for `SizedBox` and `ColoredBox`:

```clojure
(f/widget
  :height 100
  :width 200
  :color m/Colors.blue
  (m/Text "Sized and colored"))
```

#### :padding

```clojure
:padding 16                           ;; EdgeInsets.all(16)
:padding {:horizontal 8 :vertical 4}  ;; EdgeInsets.symmetric
:padding {:top 8 :bottom 16}          ;; EdgeInsets.only
```

### Cells (Derived State)

`f/$` creates a cell that recomputes when dependencies change (like a spreadsheet cell). Dependencies are read with `f/<!`:

```clojure
(let [total (f/$ (reduce + (f/<! items-atom)))]
  ...)
```

Share cells via `:bind`/`:get` rather than passing as function arguments.

## REPL

After running `clj -M:cljd flutter`, a socket REPL starts (not nREPL). Connect with:

```bash
nc localhost <port>
```

Special vars: `*1`, `*2`, `*3`, `*e`, `*env` (widget lexical bindings after `cljd.flutter.repl/pick!`).

## Entry Point Patterns

### Delegating to Dart main (hybrid project)

```clojure
(ns my-app.main
  (:require
   ["package:my_app/main.dart" :as dart-main]
   my-app.extensions.string))

(defn ^:export main []
  (dart-main/main))
```

### Pure ClojureDart main

```clojure
(ns my-app.main
  (:require
   ["package:flutter/material.dart" :as m]
   [cljd.flutter :as f]))

(defn main []
  (f/run
    (m/MaterialApp .title "My App")
    .home
    (m/Scaffold .appBar (m/AppBar .title (m/Text "My App")))
    .body
    m/Center
    (m/Text "Hello from ClojureDart!")))
```

## Common Patterns

### Conditionals

```clojure
(cond
  (<= size small-threshold) :low
  (<= size large-threshold) :standard
  :else :high)

(when (.-isNotEmpty s) (.substring s 0 1))
```

### Destructuring

```clojure
;; Map destructuring
(let [{:keys [height width]} result] ...)

;; Object destructuring
(let [{:flds [height width]} size] ...)
```

### Loop/Recur

```clojure
(loop [result s, i 0]
  (if (>= i (count items))
    result
    (recur (.replaceAll ^String result (nth items i) replacement)
           (inc i))))
```

### Try/Catch

```clojure
(try
  (DateTime/parse s)
  (catch Exception e
    (println (str "Error: " e))
    nil))
```

### Imperative Dart Object Setup

Use `doto` with `set!` to configure mutable Dart objects:

```clojure
(doto (m/Paint)
  (-> .-color (set! m/Colors.grey))
  (-> .-style (set! m/PaintingStyle.fill)))
```

## Conditional Reading

ClojureDart uses the Clojure reader, so `:clj` is always on. Put `:clj` last in reader conditionals. For macros needing Clojure host code, use `:cljd/clj-host`:

```clojure
#?(:cljd (dart-specific-code)
   :clj  (clojure-fallback))
```

## Tooling

| Tool | Command | Purpose |
|------|---------|---------|
| ClojureDart (dev) | `clj -M:cljd flutter` | Compile, watch, hot reload |
| ClojureDart (AOT) | `clj -M:cljd compile` | Compile for deployment |
| ClojureDart (init) | `clj -M:cljd init` | Initialize project |
| ClojureDart (update) | `clj -M:cljd upgrade` | Update to latest ClojureDart |
| clj-kondo | `clj-kondo --lint src` | Lint `.cljd` files |
| cljfmt | `clj -M:cljfmt fix` | Format `.cljd` files |
| Babashka | `bb script.bb` | Build scripts and automation |

## Key Rules

1. **Never edit files in `lib/cljd-out/`.** Generated by the compiler, overwritten on every compile.
2. **Use `clj -M:cljd flutter` during development.** It watches for changes and hot reloads.
3. **Use `clj -M:cljd compile` for deployment.** AOT compilation only.
4. **Fix dynamic warnings immediately.** They cascade and can cause runtime failures.
5. **Exclude `lib/cljd-out/` from Dart analysis.** Add to `analysis_options.yaml` under `analyzer > exclude`.
6. **Hyphens in namespaces, underscores in file paths.** Namespace `my-app.feature` maps to `src/my_app/feature.cljd`.
7. **Use `^:export` for functions called from Dart.** Without it, the function is not visible to Dart code.
8. **Use `^Type` hints for Dart interop.** Prevents dynamic warnings and enables correct Dart type generation.
9. **No trailing dot on constructors.** Use `(Type args)`, not `(Type. args)`.
