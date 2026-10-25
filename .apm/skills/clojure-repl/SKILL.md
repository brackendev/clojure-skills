---
name: clojure-repl
description: >-
  Use when evaluating Clojure, ClojureScript, or ClojureDart code in a running
  REPL: checking how a function behaves, reproducing a bug interactively,
  reloading a changed namespace, inspecting live application state, or running
  a test namespace from the REPL. Triggers: nREPL, `.nrepl-port`,
  `.shadow-cljs/nrepl.port`, `clj-nrepl-eval`, clojure-mcp, `clojure_eval`,
  `list_nrepl_ports`, `shadow/repl`, the ClojureDart socket REPL, `:reload`,
  "try it in the REPL", "evaluate this", or REPL-driven development. Covers
  finding and verifying the right REPL, choosing a client, evaluating small
  forms with bounded output, reloading, and confirming REPL results against the
  saved source.
user-invocable: false
---

# Clojure REPL

The `clojure` skill makes REPL-driven development the primary workflow. This skill covers the mechanics: finding the REPL that belongs to the project, evaluating forms safely, and turning a REPL result into a verified source change. The `clojure` skill's REPL conventions (fully qualified calls across namespaces, `in-ns`, and `comment` forms) still apply.

## Key Rules

1. **Verify the REPL before evaluating in it.** A port file or a listening port names only a candidate. Confirm that the process serves this project and the dialect you need (Clojure, ClojureScript, or ClojureDart): require one of the project's namespaces, and on a JVM REPL check that `(System/getProperty "user.dir")` is this checkout. A REPL from another checkout or an old build evaluates against code you are not editing. Both clients below print a line such as `*======== user | clj ========*` after each result, naming the namespace and the REPL type. Do not rely on `*ns*` in a shadow-cljs ClojureScript session, where it evaluates to `nil`.
2. **Ask before evaluating side effects outside the process.** A REPL acts on the live system it runs in. Get the user's confirmation before evaluating forms that write to a database, send network requests or messages, delete files, or start and stop services. Never evaluate in a production REPL, such as one opened with Biff's `prod-nrepl` task, without the user's explicit instruction. While `prod-nrepl` runs, it writes the production port to the project's `.nrepl-port` file, so that file then names production, not the development REPL.
3. **Evaluate small forms and bound their output.** Evaluate one function call or one expression at a time. Limit printed results with `(take 10 coll)`, `count`, or `keys`, or print inside a binding with `(binding [*print-length* 20 *print-level* 5] (pr-str expr))`. A `binding` around an expression that returns a collection does not help, because the REPL prints the result after the binding ends. To limit every result in the session, evaluate `(set! *print-length* 20)` once, and undo it with `(set! *print-length* nil)`. Set a timeout on the client so a hung evaluation does not block the session.
4. **Reload explicitly, and only what changed.** Use `(require 'my-app.core :reload)` after editing a namespace. `:reload-all` also reloads every namespace it requires and is slower. Reloading a test namespace with `:reload` does not reload the source namespace it tests, so after editing source, reload that namespace too, or reload the test namespace with `:reload-all`, before running the tests. Do not reload automatically after every edit. Reloading does not remove a var that the file no longer defines (Key Rule 5). Framework rules take precedence: in Electric, edit and save the file and let hot code reloading update the app, as the Electric starter app describes ("edit -> save -> see app reload in browser"), instead of redefining an `e/defn` at the REPL.
5. **The REPL is not the source of truth.** Iron Law CLJ-12 in the `clojure` skill applies: write the change to the source file, then run the project's tests or lint in a fresh process, or after a full reload, before reporting success. REPL state can hold definitions that no file contains, such as a function renamed or deleted since it was evaluated. Remove a stale var with `(ns-unmap 'my-app.core 'old-name)`, or restart the REPL, before trusting results that depend on it.
6. **Do not stall when there is no REPL tooling.** When no client is available and the project has no REPL running, edit the files and verify them with the project's test and lint commands, as the `clojure` skill directs.

## Choosing a client

Use the first option that is available.

| Client | When to use it | How |
|--------|----------------|-----|
| [clojure-mcp](https://github.com/bhauman/clojure-mcp) | Its tools are configured in the runtime | `list_nrepl_ports` lists the nREPL servers it finds, including shadow-cljs ones, and separates those in this directory from those in other directories. `clojure_eval` keeps a persistent session for each port. Its `port` argument selects the REPL, and `timeout_ms` (default 20000) interrupts a long evaluation. |
| [`clj-nrepl-eval`](https://github.com/bhauman/clojure-mcp-light) | `command -v clj-nrepl-eval` succeeds | `clj-nrepl-eval --discover-ports` lists the servers it finds under "In current directory" and "In other directories", with a type such as `clj` or `shadow`. A Babashka nREPL can show as `clj`. Evaluate with `clj-nrepl-eval -p <port> --timeout 10000 <<'EOF'`, then the code, then `EOF`, because a heredoc avoids shell quoting problems. The session persists between calls. `--reset-session` starts a new session but keeps defined vars and loaded namespaces. The command exits 0 even when the evaluation fails, so read its output. |
| The user's editor | Neither tool is available, but the user has a REPL open | Ask the user to evaluate the form and paste the result. |

clojure-mcp's recommended `:repl` profile (`clojure -Tmcp start :config-profile :repl`) provides only `clojure_eval`, `list_nrepl_ports`, and `paren_repair`, so read and edit files with the runtime's own tools. Unless clojure-mcp has a port of its own (a `:port` argument, or an nREPL it started through `:start-nrepl-cmd` or `:fallback-nrepl`), `clojure_eval` reads `.nrepl-port` in the project directory on every call. It therefore reaches a REPL that was started or restarted after clojure-mcp, and clojure-mcp needs no restart. Another MCP server may read the port only when it starts. When such a server cannot reach a new REPL, ask the user to restart that server, because an agent cannot restart the MCP servers of its own session.

Both clojure-mcp and `clj-nrepl-eval` repair unbalanced delimiters with Parinfer before evaluating, and neither reports the repair. Indentation decides where a repaired form closes, so a malformed form can evaluate as a different program than the one you wrote. Send balanced forms. Treat a successful evaluation as evidence about the repaired form only, never as evidence that a source file is balanced.

Neither client applies to ClojureDart, whose REPL is a plain socket REPL. `clojuredart/references/repl.md` in the `clojuredart` skill covers connecting to it.

## Finding the REPL

| Process | Where the port is |
|---------|-------------------|
| An nREPL started from the Clojure CLI (`nrepl.cmdline`) or Leiningen | `.nrepl-port` in the directory the server started in, which is the project root for Leiningen. `nrepl.cmdline` deletes the file when its JVM shuts down normally, but a process killed with `kill -9` leaves a stale file whose port refuses connections. An nREPL embedded with `nrepl.server/start-server` writes no port file. |
| shadow-cljs | `.shadow-cljs/nrepl.port`, or the `:nrepl {:port ...}` value in `shadow-cljs.edn`. shadow-cljs writes `.nrepl-port` only when `:nrepl {:write-port-file true}` is set. |
| ClojureDart (`clojure -M:cljd flutter`) | The first field of `REPL.lock` in the project directory, or the line with `ClojureDart REPL` and `listening on port` in the command output (`=== 🤫 ClojureDart REPL === listening on port === <port> ===` when the output is not a terminal). `REPL.lock` alone does not prove that a REPL is listening: `clojure -M:cljd watch` and a web target (`-d chrome`) also write it, and neither serves a REPL. |

When the project has no REPL running and the runtime can start a background process, start one with the command the host or framework skill names (for example, an `:nrepl` alias in `clojure-jvm`, the `dev` task in Biff, or the starter's development entry point in Electric). Keep nREPL on its default bind address, `127.0.0.1`: the nREPL documentation says to "always be careful not to start nREPL on a public host and port." Stop any REPL you started when the task ends, by process ID and with a normal signal such as `SIGTERM`, so that nREPL removes its port file.

## Host notes

- **JVM Clojure.** `clojure-jvm/references/project-workflows.md` in the `clojure-jvm` skill covers starting nREPL, `add-lib` for exploring a library without a restart, and running tests from the REPL.
- **ClojureScript.** `clojurescript/references/project-workflows.md` in the `clojurescript` skill covers the `cljs.main` REPLs and the shadow-cljs REPL: switching an nREPL session to a build with `(shadow/repl :build-id)`, returning with `:cljs/quit`, and the running `watch` and connected JavaScript runtime that a build REPL needs.
- **ClojureDart.** Forms evaluate in the running Dart isolate. `clojuredart/references/repl.md` in the `clojuredart` skill covers driving app state and inspecting widgets.
- **Frameworks.** Biff's `repl.clj` provides `(get-ctx)` and `(refresh)`. The Fulcro template restarts the server with `(restart)` from its `development` namespace. Electric must run the Clojure REPL and the shadow-cljs compiler in one JVM. Follow the framework skill for these workflows.

## When an evaluation fails

Read the whole error before changing anything. Inspect the exception with `ex-message`, `ex-data`, and `ex-cause`. The `clojure-debug` skill covers the procedure for finding the cause, and the host skills cover the host's tools, such as `*e` and `Throwable->map` on the JVM.
