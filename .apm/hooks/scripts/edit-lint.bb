;; edit-lint.bb - After the agent edits a Clojure family file, lints that file
;; with clj-kondo and returns the findings to the agent. Started by
;; edit-lint.sh, which reports the hook as inactive when Babashka is missing.
;;
;; Shipped as an APM hook primitive. APM registers it as a PostToolUse hook with
;; the matcher "*", because APM passes matchers through unchanged and each
;; runtime names its edit tools differently. The script picks the edit events
;; itself. Runtimes send different input and read different output:
;;
;;   Claude Code   Write and Edit, path in tool_input.file_path. Output:
;;                 hookSpecificOutput.additionalContext.
;;   Codex         apply_patch, paths in the "*** Add File:" and
;;                 "*** Update File:" lines of tool_input.command. Output:
;;                 hookSpecificOutput.additionalContext. A top-level
;;                 additionalContext makes Codex reject the output.
;;   Copilot CLI   copilot-hooks.json passes camelCase input: toolName create or
;;                 edit with the path in toolArgs.path (toolArgs can arrive as a
;;                 JSON string), or apply_patch with the patch text as toolArgs,
;;                 depending on the model. Output:
;;                 top-level additionalContext. In a trusted folder Copilot also runs the
;;                 Claude Code copy with VS Code compatible input (tool_result),
;;                 and this script stays silent on that copy so the agent does
;;                 not receive every finding twice.
;;
;; The script never edits source. clj-kondo reads the project's own
;; .clj-kondo configuration and cache. Findings already reported for a file in
;; the same session are not repeated, and a second hook process for the same
;; tool call and file content (a user-scope and a project-scope install, for
;; example) exits without output.
(ns edit-lint
  (:require [babashka.fs :as fs]
            [babashka.process :as process]
            [cheshire.core :as json]
            [clojure.string :as str]))

(def lint-extensions #{"clj" "cljs" "cljc" "cljd" "edn" "bb"})

;; Directories whose files are generated, vendored, or dependency copies. A
;; Clojure edit there is not the agent's source. .gitignore matches are
;; excluded separately.
(def excluded-dirs
  #{".git" "node_modules" "vendor" "third_party" "target" "build" "dist"
    ".cpcache" ".shadow-cljs" ".clj-kondo" "cljd-out"})

(def project-markers ["deps.edn" "project.clj" "shadow-cljs.edn" "bb.edn" ".clj-kondo"])

(def max-findings 12)

(def lint-timeout-ms 10000)

;; Input shapes

(defn copilot-input? [input] (contains? input :toolName))

(defn copilot-claude-copy? [input] (contains? input :tool_result))

(defn patch-paths
  "Files that an apply_patch command adds, updates, or moves to."
  [patch]
  (->> (str/split-lines (or patch ""))
       (keep #(second (re-matches #"\*\*\* (?:Add File|Update File|Move to): (.+)" %)))
       (map str/trim)))

(defn copilot-args
  "Copilot's toolArgs: a map, a JSON string that holds a map, or the patch text
  of its apply_patch tool."
  [args]
  (if (string? args)
    (let [parsed (try (json/parse-string args true) (catch Exception _ nil))]
      (if (map? parsed) parsed args))
    args))

(defn edited-paths
  "The files an edit event changed, or nil when the event is not an edit."
  [input]
  (if (copilot-input? input)
    (let [args (copilot-args (:toolArgs input))]
      (case (:toolName input)
        ("create" "edit") (some-> (when (map? args) (:path args)) vector)
        "apply_patch" (seq (patch-paths (if (string? args) args (:command args))))
        nil))
    (case (:tool_name input)
      ("Write" "Edit" "MultiEdit") (some-> (get-in input [:tool_input :file_path]) vector)
      "apply_patch" (seq (patch-paths (get-in input [:tool_input :command])))
      nil)))

(defn session-id [input]
  (or (:session_id input) (:sessionId input) "no-session"))

;; Files

(defn absolute-path [cwd path]
  (str (fs/normalize (fs/absolutize (if (fs/absolute? path) path (fs/path (or cwd ".") path))))))

(defn extension [path]
  (some-> (re-find #"\.([^./]+)$" path) second))

(defn project-root
  "The nearest ancestor directory with a Clojure project marker, or the file's directory."
  [path]
  (let [dir (fs/parent path)]
    (or (->> (iterate fs/parent dir)
             (take-while some?)
             (filter (fn [d] (some #(fs/exists? (fs/path d %)) project-markers)))
             first)
        dir)))

(defn git-ignored? [root path]
  (try
    (zero? (:exit (process/shell {:dir (str root) :out :string :err :string :continue true}
                                 "git" "check-ignore" "-q" "--" path)))
    (catch Exception _ false)))

(defn lintable? [root path]
  (and (lint-extensions (extension path))
       (fs/regular-file? path)
       (not-any? excluded-dirs (map str (fs/components (fs/relativize root path))))
       (not (git-ignored? root path))))

;; Session state

(defn state-dir [input]
  (let [dir (fs/path (fs/temp-dir) "clojure-skills-edit-lint"
                     (str/replace (session-id input) #"[^A-Za-z0-9_-]" "_"))]
    (fs/create-dirs dir)
    dir))

(defn sha256 [^String s]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256") (.getBytes s "UTF-8"))]
    (apply str (map #(format "%02x" %) digest))))

(defn event-id
  "Identifies one tool call. Two installs of the hook (user scope and project
  scope) receive the same tool_use_id from Claude Code and Codex. Copilot's own
  copy sends a timestamp instead."
  [input]
  (str (or (:tool_use_id input) (:timestamp input) "")))

(defn claim-content!
  "True when this process is the first to lint this file content for this tool
  call. A later edit that restores earlier content is a new tool call and is
  linted again."
  [dir input path]
  (let [marker (fs/file dir (str "content-" (sha256 (str (event-id input) "\n" path "\n" (slurp path)))))]
    (.createNewFile marker)))

(defn once!
  "True the first time a notice with this name is claimed in the session."
  [dir notice]
  (.createNewFile (fs/file dir (str "notice-" notice))))

(defn reported-file [dir path] (fs/file dir (str "reported-" (sha256 path))))

(defn read-reported [dir path]
  (let [f (reported-file dir path)]
    (if (fs/exists? f) (set (str/split-lines (slurp f))) #{})))

(defn write-reported! [dir path keys]
  (spit (reported-file dir path) (str/join "\n" (sort keys))))

;; Linting

(defn run-clj-kondo [root path]
  (let [proc (process/process {:dir (str root) :out :string :err :string}
                              "clj-kondo" "--lint" path
                              "--config" "{:output {:format :json}}")
        result (deref proc lint-timeout-ms ::timeout)]
    (if (= result ::timeout)
      (do (process/destroy-tree proc)
          {:error "clj-kondo did not finish within 10 seconds"})
      (try
        {:findings (:findings (json/parse-string (:out result) true))}
        (catch Exception _
          {:error (str "clj-kondo produced no readable output: " (str/trim (:err result)))})))))

(defn relevant-findings
  "ClojureDart files lint without ClojureDart's exported clj-kondo configuration,
  which reports Dart interop as unresolved. Only syntax errors are reliable there."
  [path findings]
  (cond->> findings
    (= "cljd" (extension path)) (filter #(= "syntax" (:type %)))))

(defn finding-key
  "Identifies a finding by its type, message, and source line, so a finding that
  only moved to another line is not reported again."
  [lines {:keys [type message row]}]
  (str type " | " message " | " (str/trim (get lines (dec (or row 1)) ""))))

(defn format-finding [{:keys [row col level type message]}]
  (format "- %s:%s %s [%s] %s" row col level type message))

(defn report-for-file [root path findings]
  (let [shown (take max-findings (sort-by (juxt #(if (= "error" (:level %)) 0 1) :row) findings))
        hidden (- (count findings) (count shown))
        unresolved? (some #(= "unresolved-symbol" (:type %)) shown)]
    (str/join "\n"
              (cond-> [(str "clj-kondo reports these problems in " (fs/relativize root path)
                            " after the latest edit. Some may predate it.")]
                true (into (map format-finding shown))
                (pos? hidden) (conj (str "- " hidden " more. Run clj-kondo --lint " path " to see them all."))
                unresolved? (conj (str "An unresolved symbol can come from a macro that clj-kondo does not know. "
                                       "Check the project's clj-kondo configuration before rewriting code that runs."))))))

(defn lint-file
  "The report for one edited file, or nil when there is nothing new to report."
  [dir root path]
  (let [{:keys [findings error]} (run-clj-kondo root path)]
    (if error
      (when (once! dir (str "lint-error-" (sha256 path)))
        (str "edit-lint could not lint " path ": " error))
      (let [lines (vec (str/split-lines (slurp path)))
            findings (relevant-findings path findings)
            previous (read-reported dir path)
            keyed (map (fn [f] [(finding-key lines f) f]) findings)
            fresh (keep (fn [[k f]] (when-not (previous k) f)) keyed)]
        (write-reported! dir path (map first keyed))
        (when (seq fresh)
          (report-for-file root path fresh))))))

;; Output

(defn respond [input text]
  (println (json/generate-string
            (if (copilot-input? input)
              {:additionalContext text}
              {:hookSpecificOutput {:hookEventName "PostToolUse" :additionalContext text}}))))

(defn -main []
  (let [input (json/parse-string (slurp *in*) true)
        paths (edited-paths input)]
    (when (and (seq paths) (not (copilot-claude-copy? input)))
      (let [dir (state-dir input)
            cwd (:cwd input)
            targets (->> paths
                         (map #(absolute-path cwd %))
                         (keep (fn [p] (let [root (project-root p)] (when (lintable? root p) [root p]))))
                         (filter (fn [[_ p]] (claim-content! dir input p))))]
        (cond
          (empty? targets) nil
          (not (fs/which "clj-kondo"))
          (when (once! dir "missing-clj-kondo")
            (respond input "edit-lint is inactive: clj-kondo is not installed, so edited Clojure files are not linted. Install it from https://github.com/clj-kondo/clj-kondo."))
          :else
          (when-let [reports (seq (keep (fn [[root p]] (lint-file dir root p)) targets))]
            (respond input (str/join "\n\n" reports))))))))

(try
  (-main)
  (catch Exception e
    ;; A hook failure must not block the agent. Report it on standard error, which
    ;; the runtimes log without treating it as a denial.
    (binding [*out* *err*]
      (println "edit-lint failed:" (ex-message e)))))
