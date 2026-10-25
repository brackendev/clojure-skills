# Reader Conditionals

Part of the `clojure` skill. This host-neutral guidance applies to portable code in `.cljc` files and to `.cljd` files. Sources: the reader reference (https://clojure.org/reference/reader, "Reader Conditionals") and the reader conditionals guide (https://clojure.org/guides/reader_conditionals).

## Where conditionals are read

The reader processes conditionals in `.cljc` files and at the REPL. A `.clj` or `.cljs` file that contains one fails with "Conditional read not allowed", and so does `read-string` unless its options map includes `:read-cond :allow`. ClojureDart reads every source file with `:read-cond :allow`, so conditionals also work in `.cljd` files.

## Platform tags

| Tag | Matches |
|-----|---------|
| `:clj` | JVM Clojure |
| `:cljs` | ClojureScript |
| `:cljr` | ClojureCLR |
| `:cljd` | ClojureDart |
| `:default` | Every platform |

## How a conditional reads

The reader checks the branches in order and reads the first one whose tag matches. `:default` always matches, so put it last: `#?(:default :a :clj :b)` reads `:a` on the JVM.

In a file that ClojureDart also compiles, put `:clj` last among the platform tags, after `:cljd` and before any `:default`. ClojureDart reads the code it compiles to Dart with only `:cljd` on, but its host pass, which runs `ns` forms and macros on the JVM, reads with `:cljd`, `:cljd/clj-host`, and `:clj` on, so a `:clj` branch that comes first wins there. `(:require #?(:clj [clojure.string :as s] :cljd [clojure.set :as s]))` fails to compile under ClojureDart 0.9.20261002, and the same form with the `:cljd` branch first compiles. A function body reads correctly in either order, because only the Dart pass compiles it, but one order everywhere is simpler.

When no tag matches and there is no `:default`, the conditional reads nothing, not `nil`. The guide: "as if nothing was read from the stream at all."

```clojure
[1 #?(:cljs 2) 3]        ; [1 3] on the JVM
{:a #?(:cljs 1)}         ; on the JVM: "Map literal must contain an even number of forms"
{:a #?(:cljs 1 :default nil)}  ; {:a nil} on the JVM
```

A missing branch can therefore leave an argument out of a call, or a value out of a map, without an error at the call site. Give each conditional a branch for every platform the file targets, or a `:default`.

## Splicing with `#?@`

`#?@` splices the elements of a collection into the surrounding form:

```clojure
[0 #?@(:clj [1 2] :cljs [3 4])]   ; [0 1 2] on the JVM, [0 3 4] in ClojureScript
```

It is not allowed at the top level of a file. The JVM reader fails with "Reader conditional splicing not allowed at the top level.", and the ClojureScript compiler with "cond-splice not in list". Use one conditional per top-level form, or wrap the forms in `do`:

```clojure
;; bad: top-level splice
#?@(:clj [(defn a [] 1) (defn b [] 2)])

;; good
#?(:clj (do (defn a [] 1)
            (defn b [] 2)))
```

## Use them sparingly

The reader reference: "Reader conditionals should be used sparingly and only when necessary." Keep host code in a few namespaces, give each host function one portable name, and call that name from the rest of the code. A file with a conditional in most forms belongs in separate host files.

## Portable patterns

### Conditional require

```clojure
(ns my-app.files
  (:require [clojure.string :as str]
            #?@(:cljd [] :clj [[clojure.java.io :as io]])))
```

The empty `:cljd` branch comes first so that ClojureDart's host pass does not read the `:clj` branch. A plain `#?(:clj [clojure.java.io :as io])` works on the JVM and in ClojureScript, but ClojureDart's host pass reads it, maps `clojure.java.io` to `cljd.java.io`, and fails to compile because that namespace does not exist.

ClojureScript's macro options (`:refer-macros`, `:include-macros`) are covered in `clojurescript/references/compilation.md` in the `clojurescript` skill (clojurescript-skills).

### Catch-all type

```clojure
(try
  (risky-call)
  (catch #?(:cljd Object :cljs :default :clj Exception) e
    (handle-error e)))
```

- **JVM Clojure.** `Exception` catches `ex-info` and the other application exceptions. `clojure-jvm/references/errors-and-resources.md` in the `clojure-jvm` skill (clojure-jvm-skills) covers why not to catch `Throwable`.
- **ClojureScript.** JavaScript can throw any value, and only `:default` catches all of them. On ClojureScript 1.12.145, `js/Error` misses a thrown string, number, keyword, or `nil`, and `js/Object` misses a thrown string, number, or `nil`.
- **ClojureDart.** Dart can throw any non-null object, and `Object` or `dynamic` catches all of them, including `ex-info`, a thrown string, and Dart `Error` values such as the `ArgumentError` from an out-of-range `nth`. `ex-info` values are not Dart `Exception`s, so `Exception` does not catch them. `(catch cljd.core/ExceptionInfo e)` catches only `ex-info` values, and the bare name `ExceptionInfo` fails to compile with "Can't resolve type ExceptionInfo." `(catch :default e)` fails to compile with "Can't resolve type :default." `clojuredart/references/errors-and-resources.md` in the `clojuredart` skill (clojuredart-skills) covers Dart's error types.

## Host notes

The host skills cover what is specific to each host: `clojurescript/references/compilation.md` in the `clojurescript` skill (clojurescript-skills), and `clojuredart/references/project-structure.md` in the `clojuredart` skill (clojuredart-skills), including ClojureDart code that runs on the JVM at compile time, such as macros.
