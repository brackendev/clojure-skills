#!/bin/bash
# edit-lint.sh - Starts edit-lint.bb with Babashka, passing the hook input
# through on standard input and any arguments, such as the event name that
# copilot-hooks.json supplies, through unchanged. APM registers this launcher
# as the hook command.
#
# The hook runs after every tool call, so the launcher first checks the input
# for a Clojure family file name and exits at once when there is none. That
# keeps the common case (shell commands, reads, searches) from starting
# Babashka.
#
# Without Babashka the hook cannot run. The launcher then says so on standard
# error and exits 0, so the session continues without the lint feedback.
input=$(cat)
case "$input" in
  *.clj* | *.edn* | *.bb*) ;;
  *) exit 0 ;;
esac
if ! command -v bb >/dev/null 2>&1; then
  echo "edit-lint is inactive: Babashka (bb) is not installed. Install it from https://babashka.org to enable this hook." >&2
  exit 0
fi
printf '%s' "$input" | exec bb "$(dirname "${BASH_SOURCE[0]}")/edit-lint.bb" "$@"
