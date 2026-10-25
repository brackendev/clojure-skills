# Untrusted Input

Part of the `clojure` skill. This host-neutral guidance applies to `.clj`, `.cljs`, `.cljc`, and `.cljd` code. It covers Iron Laws CLJ-10 and CLJ-11.

## Reading data

`clojure.core/read-string` and `read` read Clojure code, not just data. Their docstrings say they "can execute code (controlled by `*read-eval*`), and as such should be used only with trusted sources," and they point to `clojure.edn/read-string` for data. `eval` and `load-string` run whatever they are given. Parse anything that comes from outside the program (a request body, a file, a message, a database column) with an EDN reader:

| Host | EDN reader |
|------|------------|
| JVM Clojure | `clojure.edn/read-string`, `clojure.edn/read` |
| ClojureScript | `cljs.reader/read-string`, which reads EDN |
| ClojureDart | `cljd.edn/read-string` |

```clojure
;; bad: #=(...) in the input can run code
(read-string request-body)

;; good: EDN only
(edn/read-string request-body)
```

The EDN readers run the tag readers you pass them. `clojure.edn/read-string` calls any function in its `:readers` and `:default` options for tagged values in the input, so register only readers that are safe for untrusted text. Use a format library (JSON, transit) for data in those formats, and validate the parsed value before using it.

## SQL and shell commands

Joining input into a SQL string or a command line lets the input change the command. Pass values separately:

```clojure
;; bad: id can end the query and append its own SQL
(jdbc/execute! ds [(str "select * from users where id = " id)])

;; good: the driver sends id as a parameter
(jdbc/execute! ds ["select * from users where id = ?" id])
```

Parameters carry values only. OWASP notes that parameter markers "represent data values, not table names, column names, or SQL keywords," so map a user-chosen column or sort direction through an allowlist of fixed fragments:

```clojure
(def sort-columns {"name" "name" "created" "created_at"})

(let [column (get sort-columns requested "name")]
  (jdbc/execute! ds [(str "select * from users order by " column)]))
```

Joining fixed, application-owned fragments is safe. Joining input is not. For SQL builders such as HoneySQL, keep values in the data structure and let the builder produce parameters.

`clojure.java.shell/sh` passes each string it receives to the process as a separate argument, without a shell. Keep it that way:

```clojure
;; bad: the shell interprets file, so "x; rm -rf ~" runs a second command
(sh "sh" "-c" (str "wc -l " file))

;; good: file is one argument
(sh "wc" "-l" file)
```

`babashka.process` tokenizes the first string argument of `shell` and `process` on spaces, so pass each argument as its own string there too. Separate arguments still reach another program, which may treat a value that starts with `-` as an option. OWASP: "Separating the arguments does not replace validating them." Validate arguments, and put `--` before positional arguments when the program supports it.
