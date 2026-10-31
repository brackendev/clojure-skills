# Untrusted Input

Part of the `clojure` skill. This host-neutral guidance applies to `.clj`, `.cljs`, `.cljc`, and `.cljd` code. It covers Iron Laws CLJ-10 and CLJ-11. `clojure-jvm/references/untrusted-input.md` in the `clojure-jvm` skill (clojure-jvm-skills) covers the JVM libraries: next.jdbc, external commands, and the `clojure.edn` reader options. `babashka/references/untrusted-input.md` in the `babashka` skill (babashka-skills) covers external commands through `babashka.process`.

## Reading data

`clojure.core/read-string` and `read` read Clojure code, not just data. Their docstrings say they "can execute code (controlled by `*read-eval*`), and as such should be used only with trusted sources," and they point to `clojure.edn/read-string` for data. `eval` and `load-string` run whatever they are given. Parse anything that comes from outside the program (a request body, a file, a message, a database column) with an EDN reader:

| Host | EDN reader |
|------|------------|
| JVM Clojure | `clojure.edn/read-string`, `clojure.edn/read` |
| ClojureScript | `cljs.reader/read-string`, which reads EDN |
| ClojureDart | `cljd.edn/read-string` |

Portable `.cljc` code can require `[clojure.edn :as edn]` on every host: ClojureScript provides `clojure.edn` as an alias for the `cljs.reader` functions, and ClojureDart maps `clojure.edn` to `cljd.edn`.

```clojure
;; bad: #=(...) in the input can run code
(read-string request-body)

;; good: EDN only
(edn/read-string request-body)
```

An EDN reader calls the tag reader functions it has for the tagged values in the input, so register only readers that are safe for untrusted text:

- `clojure.edn/read-string` uses the default data readers (`#inst`, `#uuid`) plus the functions in its `:readers` and `:default` options.
- `cljs.reader/read-string` takes the string alone or an options map and the string. Both forms also apply every parser registered with `cljs.reader/register-tag-parser!`, so a parser that any namespace registers applies to untrusted input too.
- `cljd.edn/read-string` takes only the string.

Use a format library (JSON, transit) for data in those formats, and validate the parsed value before using it.

## SQL

Joining input into a SQL string lets the input change the query. Pass values as parameters. Clojure SQL libraries share one shape for this, a vector of a query string with `?` placeholders followed by the values, which next.jdbc accepts and HoneySQL's `format` returns:

```clojure
;; bad: id can end the query and append its own SQL
[(str "select * from users where id = " id)]

;; good: the driver sends id as a parameter
["select * from users where id = ?" id]
```

With HoneySQL, which runs on the JVM and in ClojureScript, keep values in the query map and let `format` produce the parameters:

```clojure
(require '[honey.sql :as sql])

(sql/format {:select [:*] :from [:users] :where [:= :id id]})
;; => ["SELECT * FROM users WHERE id = ?" id]
```

Parameters carry values only. OWASP notes that parameter markers "represent data values, not table names, column names, or SQL keywords," so map a user-chosen column or sort direction through an allowlist of fixed identifiers:

```clojure
(def sort-columns {"name" :name "created" :created-at})

(sql/format {:select [:*] :from [:users]
             :order-by [[(get sort-columns requested :name) :desc]]})
;; requested "created" => ["SELECT * FROM users ORDER BY created_at DESC"]
```

Joining fixed, application-owned fragments is safe. Joining input is not.

## External commands

A shell interprets a command string, so input joined into one can run a second command. Pass the program and each argument as separate strings, and never build a command string for `sh -c`:

```clojure
;; bad: the shell interprets file, so "x; rm -rf ~" runs a second command
["sh" "-c" (str "wc -l " file)]

;; good: file is one argument
["wc" "-l" file]
```

Each host's process API takes the program and its arguments separately and starts no shell by default. Node's `child_process.execFile` "does not spawn a shell by default," and Dart's `Process.run` uses a shell only when `runInShell` is true. On Windows, Dart's documentation warns that a `.bat` or `.cmd` program may still be launched through a shell, which parses its arguments by shell rules.

Separate arguments still reach another program, which may treat a value that starts with `-` as an option. OWASP: "Separating the arguments does not replace validating them." Validate arguments, and put `--` before positional arguments when the program supports it.
