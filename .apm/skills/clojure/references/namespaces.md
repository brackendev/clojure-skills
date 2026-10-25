# Namespaces, Privacy, and Metadata

Part of the `clojure` skill. This host-neutral guidance applies to `.clj`, `.cljs`, `.cljc`, and `.cljd` code.

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
- `:as-alias` when a namespace is needed only for qualified keywords: `[my-app.user :as-alias user]` makes `::user/id` read as `:my-app.user/id` without loading `my-app.user`. Clojure 1.11 and later, ClojureScript, and ClojureDart support it.
- Sort requires alphabetically
- Group requires: clojure.core libs, third-party libs, project namespaces
- Avoid single-segment namespaces (use `my-app.core`, not `my-app`)
- Hyphenated namespace segments map to underscored file paths: `my-app.core` maps to `src/my_app/core.clj` on the JVM, `src/my_app/core.cljs` for ClojureScript, `src/my_app/core.cljd` for ClojureDart

Keep namespace loading free of side effects (Iron Law CLJ-8). Loading a namespace evaluates every top-level form, in tests, at the REPL, on reload, and on the JVM during AOT compilation:

```clojure
;; bad: connects to the database whenever the namespace loads
(def conn (db/connect (System/getenv "DATABASE_URL")))

;; good: connect when the application starts
(defn start [config]
  {:conn (db/connect (:database-url config))})
```

`delay` is an acceptable middle ground for a value that should be created once, on first use.

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
