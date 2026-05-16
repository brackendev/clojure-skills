# TODO

## In Progress

## Blocked

## Next Up

- Implement the `cljd-smells-review` skill body. The current SKILL.md is a placeholder that lists planned categories (dynamic warnings, Dart interop, Flutter directive misuse, widget rebuild behavior, async patterns, generated files, project configuration). Track upstream catalog work and decide whether to ship a separate ClojureDart smells catalog or extend the existing clj-smells catalog.
- Cover `.cljd` files in the dry4clj scan used by `cljd-check`. Upstream dry4clj scans only `.clj`, `.cljc`, and `.cljs`. Build with `.cljd` added to `dry4clj.core/source-extensions` (see brackendev/dry4clj branch `add-cljd-extension`, upstream PR #1) before enabling `.cljd` duplicate detection.
- Wire `clj-smells-reviewer` into the `code-lenses` plugin's `/review-all` command behind an explicit `+clj-smells` opt-in.
