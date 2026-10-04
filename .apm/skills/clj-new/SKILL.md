---
name: clj-new
description: "Scaffold a new Clojure project with deps.edn"
argument-hint: "<project-name>"
user-invocable: true
disable-model-invocation: true
---

# Scaffold a Clojure Project

Create a new Clojure project with deps.edn, source layout, test runner, and formatting configured.

## Arguments

| Input             | Target                                                                       |
|-------------------|------------------------------------------------------------------------------|
| `<project-name>`  | Required. Use hyphens (for example, `my-app`); the skill maps to underscores for file paths (`my_app/`) and keeps hyphens in namespace symbols. |
| (no argument)     | Prompt the operator for a project name.                                      |

This skill is exempt from the `all` and `<path>` rows of the standard scope vocabulary because scaffolding has no useful default scope. The package's [CONVENTIONS.md](https://github.com/brackendev/clojure-skills/blob/master/CONVENTIONS.md) defines the standard.

## Mutation

Mutates by default: creates the project directory and writes `deps.edn`, `build.clj`, the entry-point and test source files, `.cljfmt.edn`, `.gitignore`, and a `resources/` directory with a `.gitkeep`. No `--report` flag; preview the side effects by reading this `SKILL.md`.

## Prerequisites

Verify these are installed before proceeding. If any are missing, stop and tell the user.

- `clj` (Clojure CLI / tools.deps)
- `clj-kondo` (Clojure linter)

Check with:

```bash
clj --version
clj-kondo --version
```

## Steps

### 1. Get Project Name

Use `$ARGUMENTS` as the project name. If empty, ask the user for a project name.

The project name uses hyphens (for example, `my-app`). File paths use underscores (for example, `my_app/`).

### 2. Create Project Directory and deps.edn

```bash
mkdir <project-name>
```

Create `<project-name>/deps.edn`:

```clojure
{:paths ["src" "resources"]
 :deps  {org.clojure/clojure {:mvn/version "1.12.6"}}
 :aliases
 {:dev     {:extra-paths ["dev"]}
  :test    {:extra-paths ["test"]
            :extra-deps  {io.github.cognitect-labs/test-runner
                          {:git/tag "v0.5.1" :git/sha "dfb30dd"}}
            :main-opts   ["-m" "cognitect.test-runner"]
            :exec-fn     cognitect.test-runner.api/test}
  :build   {:deps        {io.github.clojure/tools.build
                          {:git/tag "v0.10.14" :git/sha "1176afd"}}
            :ns-default  build}
  :cljfmt  {:extra-deps  {dev.weavejester/cljfmt {:mvn/version "0.16.6"}}
            :main-opts   ["-m" "cljfmt.main"]}}}
```

### 3. Create Entry Point

Create `src/<project_name>/core.clj` where `<project_name>` uses underscores:

```clojure
(ns <namespace>.core
  (:gen-class))

(defn -main
  [& _args]
  (println "Hello from <project-name>!"))
```

Where `<namespace>` uses hyphens matching the project name.

### 4. Create Test File

Create `test/<project_name>/core_test.clj`:

```clojure
(ns <namespace>.core-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [<namespace>.core :as core]))

(deftest hello-test
  (testing "main runs without error"
    (is (nil? (core/-main)))))
```

### 5. Create build.clj

Create `<project-name>/build.clj`:

```clojure
(ns build
  (:require
   [clojure.tools.build.api :as b]))

(def lib '<namespace>/<namespace>)
(def version "0.1.0")
(def class-dir "target/classes")
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis @basis
                   :ns-compile ['<namespace>.core]
                   :class-dir class-dir})
  (b/uber {:class-dir class-dir
            :uber-file uber-file
            :basis @basis
            :main '<namespace>.core}))
```

### 6. Create .cljfmt.edn

```clojure
{:paths ["src" "test"]
 :extra-indents {ns [[:inner 0]]
                 defn [[:inner 0]]
                 fn [[:inner 0]]}}
```

### 7. Create .gitignore

```
target/
.cpcache/
.nrepl-port
*.jar
```

### 8. Create resources Directory

```bash
mkdir -p <project-name>/resources
```

Create an empty `.gitkeep` inside it so the directory is tracked.

### 9. Run Tests

```bash
cd <project-name>
clj -X:test
```

Verify the test passes.

### 10. Report

Print the created files and next steps:

```
Created:
  deps.edn
  build.clj
  src/<project_name>/core.clj
  test/<project_name>/core_test.clj
  .cljfmt.edn
  .gitignore
  resources/

Next steps:
  cd <project-name>
  clj                            # Start a REPL
  clj -X:test                    # Run tests
  clj -M:cljfmt fix              # Format source files
  clj-kondo --lint src test      # Lint source files
  clj -T:build uber              # Build an uberjar
```
