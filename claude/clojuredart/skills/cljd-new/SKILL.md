---
name: cljd-new
description: Scaffold a new ClojureDart Flutter project
argument-hint: <project-name>
user-invocable: true
disable-model-invocation: true
effort: auto
---

# Scaffold a ClojureDart Flutter Project

Create a new Flutter project with ClojureDart configured and ready to compile.

## Prerequisites

Verify these are installed before proceeding. If any are missing, stop and tell the user.

- `flutter` (Flutter SDK)
- `clj` (Clojure CLI / tools.deps)
- `clj-kondo` (ClojureDart linter)

Check with:

```bash
flutter --version
clj --version
clj-kondo --version
```

## Steps

### 1. Get Project Name

Use `$ARGUMENTS` as the project name. If empty, ask the user for a project name.

The project name must be a valid Dart/Flutter package name: lowercase, underscores allowed, no hyphens.

### 2. Create Project Directory and deps.edn

Create the project directory and `deps.edn` file.

Fetch the latest ClojureDart commit SHA:

```bash
git ls-remote https://github.com/tensegritics/ClojureDart.git HEAD
```

The namespace for `:main` should match the project name with hyphens replacing underscores (e.g., project `my_app` uses namespace `my-app.main`).

```bash
mkdir <project-name>
```

Create `<project-name>/deps.edn`:

```clojure
{:paths     ["src"]
 :deps      {org.clojure/clojure {:mvn/version "1.12.0"}
             tensegritics/clojuredart
             {:git/url "https://github.com/tensegritics/ClojureDart.git"
              :sha     "<latest-sha>"}}
 :aliases   {:cljd {:main-opts ["-m" "cljd.build"]}
             :cljfmt {:extra-deps {dev.weavejester/cljfmt {:mvn/version "0.13.0"}}
                      :main-opts ["-m" "cljfmt.main"]}}
 :cljd/opts {:kind :flutter
             :main <namespace>.main}}
```

### 3. Initialize the Project

Run `clj -M:cljd init` from inside the project directory. This creates the Flutter project structure (pubspec.yaml, lib/, android/, ios/, etc.):

```bash
cd <project-name>
clj -M:cljd init
```

### 4. Create Entry Point

Create `src/<project_name>/main.cljd` with a basic Flutter app:

```clojure
(ns <namespace>.main
  (:require
   ["package:flutter/material.dart" :as m]
   [cljd.flutter :as f]))

(defn main []
  (f/run
    (m/MaterialApp .title "<Project Name>")
    .home
    (m/Scaffold
      .appBar (m/AppBar .title (m/Text "<Project Name>")))
    .body
    m/Center
    (m/Text "Hello from ClojureDart!"
      .style (m/TextStyle .fontSize 24.0))))
```

Where `<namespace>` uses hyphens (Clojure convention) and `<project_name>` uses underscores (Dart convention).

### 5. Create `.cljfmt.edn`

```clojure
{:paths ["src"]
 :indents {ns [[:inner 0]]
           defn [[:inner 0]]
           fn [[:inner 0]]}}
```

### 6. Update `analysis_options.yaml`

Add `lib/cljd-out/**` to the analyzer exclude list. Read the existing file first and add the entry, preserving existing content.

### 7. Update `.gitignore`

Append these entries if not already present:

```
# ClojureDart
lib/cljd-out/
.cpcache/
```

### 8. Compile and Run

```bash
clj -M:cljd flutter
```

This compiles ClojureDart, watches for changes, and hot reloads. The first run downloads ClojureDart dependencies and may take a minute.

### 9. Report

Print the created files and next steps:

```
Created:
  deps.edn
  src/<project_name>/main.cljd
  .cljfmt.edn
  Updated analysis_options.yaml
  Updated .gitignore

Next steps:
  cd <project-name>
  clj -M:cljd flutter       # Dev mode with hot reload
  # Edit .cljd files in src/ -- changes hot reload automatically
  clj -M:cljd compile       # AOT compile for deployment
  clj -M:cljd upgrade       # Update ClojureDart version
```
