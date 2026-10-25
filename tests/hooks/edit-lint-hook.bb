;; Runs edit-lint.sh end to end against temporary projects and checks what it
;; returns for each runtime's input: Claude Code, Codex, and both Copilot CLI
;; copies. Covers malformed forms, custom macros, reader conditionals, tagged
;; literals, ClojureDart, nested projects, project clj-kondo configuration,
;; excluded paths, repeated events, and missing executables.
;;
;; Usage: bb edit-lint-hook.bb <hook scripts directory>
(require '[babashka.fs :as fs]
         '[cheshire.core :as json]
         '[clojure.string :as str])

(def here (fs/parent (fs/absolutize *file*)))
(load-file (str (fs/path here "support.bb")))
(alias 'support 'hook-test-support)

(def scripts (or (first *command-line-args*)
                 (do (println "Usage: bb edit-lint-hook.bb <hook scripts directory>")
                     (System/exit 1))))
(def hook (str (fs/path scripts "edit-lint.sh")))

(defn session [] (str "test-" (random-uuid)))

(defn run
  "Runs the hook and returns {:exit :context :shape :err}. :shape is :nested for
  hookSpecificOutput, :top-level for a top-level additionalContext, :none for no
  output, and :invalid when standard output is not one JSON object."
  [input & opts]
  (let [{:keys [exit out err]} (apply support/run-hook hook input opts)
        out (str/trim (or out ""))]
    (if (str/blank? out)
      {:exit exit :shape :none :err err}
      (let [parsed (try (json/parse-string out true) (catch Exception _ nil))]
        (cond
          (get-in parsed [:hookSpecificOutput :additionalContext])
          {:exit exit :shape :nested :context (get-in parsed [:hookSpecificOutput :additionalContext]) :err err}
          (:additionalContext parsed)
          {:exit exit :shape :top-level :context (:additionalContext parsed) :err err}
          :else {:exit exit :shape :invalid :out out :err err})))))

(defn claude-edit
  "A Claude Code Write event. Each call is a new tool call unless an id is given."
  ([s path] (claude-edit s path (str (random-uuid))))
  ([s path tool-use-id]
   (support/claude-input s "Write" {"file_path" path "content" ""} "tool_use_id" tool-use-id)))

(defn project
  "A temporary git project with deps.edn and an optional .clj-kondo configuration."
  [& {:keys [kondo-config]}]
  (let [dir (support/temp-directory)]
    (support/git dir "init" "-q")
    (support/write-file dir "deps.edn" "{:paths [\"src\"]}\n")
    (when kondo-config (support/write-file dir ".clj-kondo/config.edn" kondo-config))
    dir))

;; A malformed form is reported to Claude Code with the reader error.
(let [dir (project)
      file (support/write-file dir "src/app/core.clj" "(ns app.core)\n\n(defn f [x]\n  (+ x 1)\n")
      {:keys [exit shape context]} (run (claude-edit (session) file))]
  (support/result 0 exit "malformed form: exit status")
  (support/result :nested shape "malformed form: Claude Code output shape")
  (support/check (and context (str/includes? context "src/app/core.clj")
                      (str/includes? context "[syntax]"))
                 (str "malformed form: context names the file and the syntax error: " context)))

;; A clean file produces no output.
(let [dir (project)
      file (support/write-file dir "src/app/core.clj" "(ns app.core)\n\n(defn f [x]\n  (+ x 1))\n")]
  (support/result :none (:shape (run (claude-edit (session) file))) "clean file: no output"))

;; An unresolved symbol is reported with the macro caution.
(let [dir (project)
      file (support/write-file dir "src/app/core.clj" "(ns app.core)\n\n(defn f []\n  (frob 1))\n")
      {:keys [context]} (run (claude-edit (session) file))]
  (support/check (and context (str/includes? context "unresolved-symbol")
                      (str/includes? context "macro that clj-kondo does not know"))
                 (str "unresolved symbol: finding and macro caution: " context)))

;; A symbol defined by a custom macro is not reported when the project's
;; clj-kondo configuration explains the macro.
(let [dir (project :kondo-config "{:lint-as {app.macros/defthing clojure.core/def}}\n")
      _ (support/write-file dir "src/app/macros.clj" "(ns app.macros)\n\n(defmacro defthing [n v] `(def ~n ~v))\n")
      file (support/write-file dir "src/app/core.clj"
                               "(ns app.core\n  (:require [app.macros :refer [defthing]]))\n\n(defthing answer 42)\n\n(defn f [] answer)\n")]
  (support/result :none (:shape (run (claude-edit (session) file)))
                  "custom macro with :lint-as configuration: no output"))

;; The project's configuration is honored: a disabled linter stays quiet.
(let [dir (project :kondo-config "{:linters {:unused-binding {:level :off}}}\n")
      file (support/write-file dir "src/app/core.clj" "(ns app.core)\n\n(defn f [x y]\n  x)\n")]
  (support/result :none (:shape (run (claude-edit (session) file)))
                  "disabled linter in project configuration: no output"))

;; Reader conditionals and tagged literals lint cleanly.
(let [dir (project)
      cljc (support/write-file dir "src/app/shared.cljc"
                               "(ns app.shared)\n\n(defn now []\n  #?(:clj (System/currentTimeMillis)\n     :cljs (.now js/Date)))\n")
      tagged (support/write-file dir "src/app/data.clj"
                                 "(ns app.data)\n\n(def start #inst \"2026-01-01T00:00:00Z\")\n(def id #uuid \"00000000-0000-0000-0000-000000000000\")\n")]
  (support/result :none (:shape (run (claude-edit (session) cljc))) "reader conditionals: no output")
  (support/result :none (:shape (run (claude-edit (session) tagged))) "tagged literals: no output"))

;; ClojureDart: Dart interop is not reported, but a reader error is.
(let [dir (project)
      interop (support/write-file dir "src/app/main.cljd"
                                  "(ns app.main\n  (:require [\"package:flutter/material.dart\" :as m]))\n\n(defn view [] (m/Text \"hi\"))\n")
      broken (support/write-file dir "src/app/broken.cljd" "(ns app.broken)\n\n(defn view [] (m/Text \"hi\")\n")]
  (support/result :none (:shape (run (claude-edit (session) interop))) "cljd interop: no output")
  (support/check (some-> (:context (run (claude-edit (session) broken))) (str/includes? "[syntax]"))
                 "cljd reader error: reported"))

;; A nested project uses its own clj-kondo configuration.
(let [dir (project)
      _ (support/write-file dir "sub/deps.edn" "{:paths [\"src\"]}\n")
      _ (support/write-file dir "sub/.clj-kondo/config.edn" "{:linters {:unresolved-symbol {:level :off}}}\n")
      file (support/write-file dir "sub/src/app/core.clj" "(ns app.core)\n\n(defn f [] (frob 1))\n")]
  (support/result :none (:shape (run (claude-edit (session) file)))
                  "nested project: its configuration disables the finding"))

;; The same event twice in one session is reported once, as when a user-scope
;; and a project-scope install both run.
(let [dir (project)
      file (support/write-file dir "src/app/core.clj" "(ns app.core)\n\n(defn f [] (frob 1))\n")
      s (session)]
  (support/result :nested (:shape (run (claude-edit s file "tool-1"))) "duplicate event: first report")
  (support/result :none (:shape (run (claude-edit s file "tool-1"))) "duplicate event: second run is silent"))

;; Restoring content that was already reported, after a fix, is reported again.
(let [dir (project)
      bad "(ns app.core)\n\n(defn f [] (frob 1))\n"
      file (support/write-file dir "src/app/core.clj" bad)
      s (session)
      _ (run (claude-edit s file))
      _ (spit file "(ns app.core)\n\n(defn f [] 1)\n")
      fixed (run (claude-edit s file))
      _ (spit file bad)
      restored (run (claude-edit s file))]
  (support/result :none (:shape fixed) "restored content: the fix is clean")
  (support/check (some-> (:context restored) (str/includes? "frob"))
                 (str "restored content: the restored error is reported again: " restored)))

;; A finding already reported is not repeated after an unrelated edit, and a
;; new finding is.
(let [dir (project)
      file (support/write-file dir "src/app/core.clj" "(ns app.core)\n\n(defn f [] (frob 1))\n")
      s (session)
      _ (run (claude-edit s file))
      _ (spit file "(ns app.core)\n\n(defn f [] (frob 1))\n\n(defn g [] (blarg 2))\n")
      {:keys [context]} (run (claude-edit s file))]
  (support/check (and context (str/includes? context "blarg") (not (str/includes? context "frob")))
                 (str "repeated finding: only the new one is reported: " context)))

;; Codex: every file in an apply_patch is linted, and the output is nested.
(let [dir (project)
      _ (support/write-file dir "src/app/a.clj" "(ns app.a)\n\n(defn f [] (frob 1))\n")
      b (support/write-file dir "src/app/b.clj" "(ns app.b)\n\n(defn g [x]\n")
      patch (str "*** Begin Patch\n*** Add File: src/app/a.clj\n+(ns app.a)\n"
                 "*** Update File: " b "\n@@\n-x\n+y\n*** End Patch")
      {:keys [shape context]} (run (support/codex-input (session) dir patch))]
  (support/result :nested shape "codex apply_patch: nested output shape")
  (support/check (and context (str/includes? context "src/app/a.clj") (str/includes? context "src/app/b.clj"))
                 (str "codex apply_patch: both files reported: " context)))

;; Copilot: its own copy gets a top-level additionalContext, and the Claude Code
;; copy that Copilot also runs stays silent.
(let [dir (project)
      file (support/write-file dir "src/app/core.clj" "(ns app.core)\n\n(defn f [] (frob 1))\n")]
  (support/result :top-level (:shape (run (support/copilot-input (session) "edit" file) :args ["postToolUse"]))
                  "copilot camelCase input: top-level output shape")
  (support/result :none (:shape (run (support/copilot-claude-copy-input (session) file)))
                  "copilot running the Claude Code copy: silent")
  (let [{:keys [shape context]} (run (support/copilot-patch-input
                                      (session) dir "*** Begin Patch\n*** Add File: src/app/core.clj\n+(ns app.core)\n*** End Patch\n")
                                     :args ["postToolUse"])]
    (support/result :top-level shape "copilot apply_patch: top-level output shape")
    (support/result :top-level
                    (:shape (run (json/generate-string {"sessionId" (session) "timestamp" 0 "cwd" dir "toolName" "edit"
                                                        "toolArgs" (json/generate-string {"path" file})})
                                 :args ["postToolUse"]))
                    "copilot toolArgs as a JSON string: the edit is linted")
    (support/check (some-> context (str/includes? "unresolved-symbol"))
                   (str "copilot apply_patch: the patched file is reported: " context))))

;; Events that are not edits of project Clojure files are ignored.
(let [dir (project)
      file (support/write-file dir "src/app/core.clj" "(ns app.core)\n\n(defn f [] (frob 1))\n")
      readme (support/write-file dir "README.md" "# app\n")
      target (support/write-file dir "target/classes/app/gen.clj" "(ns app.gen)\n(defn f [] (frob 1))\n")
      ignored (support/write-file dir "scratch/tmp.clj" "(ns tmp)\n(defn f [] (frob 1))\n")]
  (support/write-file dir ".gitignore" "scratch/\n")
  (support/result :none (:shape (run (support/claude-input (session) "Read" {"file_path" file})))
                  "read of a Clojure file: no output")
  (support/result :none (:shape (run (claude-edit (session) readme))) "edit of a Markdown file: no output")
  (support/result :none (:shape (run (claude-edit (session) target))) "edit under target/: no output")
  (support/result :none (:shape (run (claude-edit (session) ignored))) "edit of a gitignored file: no output")
  (support/result :none (:shape (run (support/claude-input (session) "Bash" {"command" "ls"})))
                  "shell command: no output"))

;; Without clj-kondo the hook says so once per session.
(let [dir (project)
      file (support/write-file dir "src/app/core.clj" "(ns app.core)\n\n(defn f [] (frob 1))\n")
      bin (support/temp-directory)
      _ (doseq [tool ["bb" "git" "cat" "dirname"]] (fs/create-sym-link (fs/path bin tool) (fs/which tool)))
      s (session)
      first-run (run (claude-edit s file) :path (str bin))
      _ (spit file "(ns app.core)\n\n(defn f [] (frob 2))\n")
      second-run (run (claude-edit s file) :path (str bin))]
  (support/check (some-> (:context first-run) (str/includes? "clj-kondo is not installed"))
                 (str "missing clj-kondo: notice on first edit: " first-run))
  (support/result :none (:shape second-run) "missing clj-kondo: no repeated notice"))

;; Without Babashka the launcher exits 0 and reports the hook as inactive on
;; standard error.
(let [dir (project)
      file (support/write-file dir "src/app/core.clj" "(ns app.core)\n")
      bin (support/temp-directory)
      _ (doseq [tool ["cat" "dirname"]] (fs/create-sym-link (fs/path bin tool) (fs/which tool)))
      {:keys [exit shape err]} (run (claude-edit (session) file) :path (str bin))]
  (support/result 0 exit "missing bb: exit status")
  (support/result :none shape "missing bb: no standard output")
  (support/check (str/includes? (or err "") "Babashka (bb) is not installed") "missing bb: inactive notice"))

(support/report "edit-lint hook")
