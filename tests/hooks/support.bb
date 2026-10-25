;; Helpers shared by the hook test suites: hook input, running a hook, temporary
;; projects, and pass and fail counting. Each suite loads this file with
;; load-file.
(ns hook-test-support
  (:require [babashka.fs :as fs]
            [babashka.process :as process]
            [cheshire.core :as json]
            [clojure.string :as str]))

(defn claude-input
  "The JSON Claude Code sends to a PostToolUse hook."
  [session tool tool-input & {:as extra}]
  (json/generate-string (merge {"session_id" session
                                "hook_event_name" "PostToolUse"
                                "tool_name" tool
                                "tool_input" tool-input
                                "tool_response" {}}
                               extra)))

(defn codex-input
  "The JSON Codex sends to a PostToolUse hook after an apply_patch call."
  [session cwd patch]
  (json/generate-string {"session_id" session
                         "turn_id" "test-turn"
                         "cwd" cwd
                         "hook_event_name" "PostToolUse"
                         "tool_name" "apply_patch"
                         "tool_input" {"command" patch}
                         "tool_response" {}}))

(defn copilot-input
  "The camelCase JSON Copilot CLI sends to a hook from copilot-hooks.json."
  [session tool path]
  (json/generate-string {"sessionId" session
                         "timestamp" 0
                         "cwd" "/"
                         "toolName" tool
                         "toolArgs" {"path" path}
                         "toolResult" {"resultType" "success" "textResultForLlm" ""}}))

(defn copilot-patch-input
  "The camelCase JSON Copilot CLI sends after its apply_patch tool, whose
  toolArgs is the patch text."
  [session cwd patch]
  (json/generate-string {"sessionId" session
                         "timestamp" 0
                         "cwd" cwd
                         "toolName" "apply_patch"
                         "toolArgs" patch
                         "toolResult" {"resultType" "success" "textResultForLlm" ""}}))

(defn copilot-claude-copy-input
  "The VS Code compatible JSON Copilot CLI sends when it runs the Claude Code
  copy of a hook in a trusted folder."
  [session path]
  (json/generate-string {"hook_event_name" "PostToolUse"
                         "session_id" session
                         "timestamp" "2026-10-25T00:00:00Z"
                         "cwd" "/"
                         "tool_name" "Edit"
                         "tool_input" {"file_path" path}
                         "tool_result" {"result_type" "success" "text_result_for_llm" ""}}))

(defn run-hook
  "Runs a hook script with input on standard input. Returns :exit, :out, and :err."
  [script input & {:keys [env path args]}]
  (let [env (cond-> (merge (into {} (System/getenv)) env)
              path (assoc "PATH" path))]
    (apply process/sh {:in input :env env :err :string} (str script) args)))

(defn temp-directory []
  (str (fs/create-temp-dir)))

(defn git [dir & arguments]
  (let [{:keys [exit err]} (apply process/sh {:dir dir :err :string} "git" arguments)]
    (when-not (zero? exit)
      (throw (ex-info (str "git " (str/join " " arguments) " failed: " err) {})))))

(defn write-file
  "Writes content to a path under dir, creating parent directories. Returns the absolute path."
  [dir relative content]
  (let [path (fs/path dir relative)]
    (fs/create-dirs (fs/parent path))
    (spit (str path) content)
    (str (fs/absolutize path))))

(def results (atom {:pass 0 :fail 0}))

(defn result
  "Records one case. Prints the description when got differs from want."
  [want got description]
  (if (= want got)
    (swap! results update :pass inc)
    (do (swap! results update :fail inc)
        (println (str "FAIL want=" (pr-str want) " got=" (pr-str got) "  " description)))))

(defn check [ok? description]
  (result true (boolean ok?) description))

(defn report
  "Prints the totals for a suite and exits 0 only when every case passed."
  [suite]
  (let [{:keys [pass fail]} @results]
    (println (str suite ": " pass " pass, " fail " fail"))
    (System/exit (if (and (pos? pass) (zero? fail)) 0 1))))
