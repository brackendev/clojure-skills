---
name: clojure-debug
description: >-
  Use when a Clojure, ClojureScript, or ClojureDart program, REPL evaluation,
  build, or test fails with an error: a stack trace, an exception, a compiler
  or macroexpansion error, or `ex-info` data in output. Triggers: "Syntax error
  compiling", "Syntax error macroexpanding", "Syntax error reading source",
  "Execution error", "Error printing return value", "Unable to resolve
  symbol", "Could not locate ... on classpath", "did not conform to spec",
  "Full report at", `ExceptionInfo`,
  `CompilerException`, `ex-data`, `ex-cause`, `*e`, a `NullPointerException`
  or `ClassCastException` in Clojure code, or a failing test that reports an
  error rather than a failed assertion. Covers keeping the original evidence,
  separating the error phase and wrapper exceptions from the root cause,
  reading `ex-data`, finding the first application frame, reproducing the
  failure, and verifying a focused fix.
user-invocable: false
---

# Clojure Debugging

This skill covers finding the cause of a Clojure family error. The procedure applies on every host. The host notes at the end point to the host skills for error formats and tools.

## Key Rules

1. **Keep the original evidence.** Save the full error output before changing anything, for example with `2>&1 | tee /tmp/error.log`. A rerun after an edit can produce a different error and erase the first one.
2. **Find the phase before the cause.** A Clojure error happens while reading source, while expanding a macro, while compiling, or while executing. Each phase has different causes, so a fix that suits one phase does not suit another.
3. **Read the whole cause chain.** An exception can wrap another exception. The outermost message often describes where the error surfaced, and the innermost cause describes what went wrong. Read every message and every `ex-data` map in the chain before deciding.
4. **Start from the first frame in the project's own code.** Stack traces are dominated by `clojure.core`, the compiler, and library frames. The first frame in a project namespace is where to start reading, though the defect can still be in the data that reached it.
5. **Reproduce before fixing.** Reproduce the failure with the smallest call that shows it: one REPL evaluation, one test, or one command. A fix that cannot be shown to change the reproduction is a guess.
6. **Change one thing, then rerun the reproduction.** Confirm the original error is gone and no new error appeared, then run the surrounding tests.
7. **Report what the evidence shows.** Separate what the error output proves from what you infer, and do not describe a hypothesis as the cause until the reproduction confirms it.

## Error phases

A Clojure error happens in one of these phases:

- **Read.** The reader cannot parse the source, usually because of unbalanced delimiters or an invalid literal.
- **Macroexpansion.** A macro call has the wrong shape, for example an odd number of `let` bindings, or the macro itself threw while expanding.
- **Compile.** A symbol cannot be resolved, often because of a missing `:require` or `:refer`, a typo, or a use before definition.
- **Execution.** The code ran and threw. The location in the message is where the error surfaced, not necessarily where the defect is.
- **Printing.** At a REPL, the expression returned, and the error came while printing the result. A common cause is a lazy sequence that is realized only when printed, and then the defect is in the code that produced the sequence. JVM Clojure reports this phase as "Error printing return value".

The host's error format names the phase. The host notes below say where to read it. The messages this skill's description quotes, such as "Syntax error compiling", "Execution error", and "Full report at", are the JVM Clojure format, and `NullPointerException` and `ClassCastException` are JVM exception types. `cljs.main` runs its compiler on the JVM and reports compiler errors in the same format, as in "Unexpected error (ExceptionInfo) compiling at (REPL:1)." ClojureDart reports a compiler error as "Compilation error" followed by sections such as "Faulty subform and/or expansion".

## Inspecting exceptions

These functions exist on every Clojure host:

```clojure
(ex-message e)  ; the message
(ex-data e)     ; the data map of an ex-info exception, or nil
(ex-cause e)    ; the wrapped exception, or nil
```

Walk the chain with `ex-cause` until it returns `nil`, and read `ex-message` and `ex-data` at every level:

```clojure
(->> e (iterate ex-cause) (take-while some?) (map (juxt ex-message ex-data)))
```

When the code throws its own errors, use `ex-info` with a data map that names the inputs that failed, and pass the original exception as the third argument when rethrowing so the chain keeps the root cause.

## Host notes

- **JVM Clojure.** `clojure-jvm/references/debugging.md` in the `clojure-jvm` skill covers the Clojure CLI error format and its phases, the "Full report at" file, `*e` and `Throwable->map`, classpath errors, and the common JVM exception types.
- **ClojureScript.** Compiler errors and warnings come from the build tool (shadow-cljs, figwheel-main, or `cljs.main`). Runtime errors appear in the browser console or the `node` output, and source maps map their frames back to ClojureScript. The `clojurescript` skill covers source maps and `catch :default`.
- **ClojureDart.** Compiler dynamic warnings exit with status 0 but signal type problems. The `clojuredart` skill's Gotchas describe how to tell an inference failure from a resolution failure.
