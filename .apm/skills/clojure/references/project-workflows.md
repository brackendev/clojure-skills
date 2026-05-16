# Project Workflows

Reference for CLI commands, MCP integration, project structure, configuration, tooling, and REPL practices.

## MCP Integration

This skill is designed to work with [clojure-mcp](https://github.com/bhauman/clojure-mcp), an MCP server for REPL-driven Clojure development. When clojure-mcp is available, use its tools for REPL evaluation, namespace reloading, and file operations instead of shell commands.

If clojure-mcp tools are not available and an `:nrepl` alias exists, start nREPL in the background with `clj -M:nrepl &` and restart the session so the MCP server can connect.

## CLI

```bash
clj                          # Start a REPL
clj -M:alias                 # Run with alias
clj -X:alias fn-name         # Execute a function
clj -T:build task            # Run a tools.build task
clj -Sdeps '{:deps {...}}'   # Add inline dependencies
```

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

Reload namespaces with the `:reload` flag to work with the latest code:

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

Reload namespaces before running tests:

```clojure
(require '[my-app.core-test] :reload)
(clojure.test/run-tests 'my-app.core-test)
```

For full test suite runs, prefer the CLI:

```bash
clj -X:test
```

### comment forms

Use `(comment ...)` forms for REPL-driven development. The compiler ignores them, but they serve as a scratchpad for interactive evaluation:

```clojure
(comment
  (require '[my-app.core :as core] :reload)
  (core/process-data sample-input)
  (time (core/expensive-operation))
  )
```

Place the closing parenthesis on its own line so the last form is easy to evaluate independently.
