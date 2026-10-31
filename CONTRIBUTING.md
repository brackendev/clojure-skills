# Contributing

For people working on the plugin source. End-user install instructions live in [README.md](README.md).

## Layout

| Path | Purpose |
|------|---------|
| `apm.yml` | Package manifest (`type: skill`, no MCP dependencies). |
| `.apm/skills/<name>/` | Canonical skill source. APM deploys from here. Each directory holds `SKILL.md` plus optional `agents/openai.yaml`, `references/`, `scripts/`, `examples/`. |
| `.apm/hooks/` | Hook definitions. `edit-lint.json` and `clj-rules.json` hold the generic copies, `copilot-hooks.json` holds the Copilot CLI copy of `edit-lint` only, and `cursor-hooks.json` is empty. `scripts/` holds each hook's bash launcher and Babashka script, and `clj-rules.md`, the condensed Iron Laws that `clj-rules` delivers. |
| `tests/hooks/` | Hook test suites. They are repository files, not package content, and APM does not deploy them. |
| `.opencode/skills` | Symlink to `../.apm/skills`. OpenCode finds it from any directory in the repository, so an OpenCode session loads the canonical skills and their supporting files without an APM install. |
| `opencode.jsonc` | Local OpenCode configuration. |
| `README.md` | End-user documentation. |
| `CHANGELOG.md` | User-facing changes per version. |
| `CONVENTIONS.md` | Canonical argument grammar, scope vocabulary, and mutation defaults for every user-invocable skill. |
| `AGENTS.md`, `GEMINI.md`, `TODO.md` | Local agent instructions (`GEMINI.md` is a symlink to `AGENTS.md`) and working notes. Gitignored globally and never committed. |

## APM lockfile rule

Do not add `.claude-plugin/`, `.codex-plugin/`, `.agents/plugins/marketplace.json`, or a root `plugin.json`. Any of those change the APM lockfile classification from `apm_package` to `marketplace_plugin` and suppress skill deployment to every runtime.

## Adding or modifying a skill

1. Edit `.apm/skills/<name>/SKILL.md`.
2. Update `README.md` if the change is user-facing.
3. Add a `CHANGELOG.md` entry for every change to a published file, including documentation-only changes, under a `## [x.y.z] - YYYY-MM-DD` heading for the new `apm.yml` version. Leave `## [Unreleased]` empty above it.
4. Increment the `version` field in `apm.yml`.

## Adding or modifying a hook

1. Edit the hook JSON in `.apm/hooks/<name>.json`, keyed by lifecycle event (`PostToolUse`). Use the matcher `"*"` and choose the events inside the script, because APM passes matchers through unchanged and each runtime names its tools differently.
2. Add the same hook to `.apm/hooks/copilot-hooks.json` in Copilot's format: a camelCase event and a flat entry with `type`, `bash`, and `timeoutSec`. Pass the event name to the script as an argument, because Copilot's input does not name it. Keep `.apm/hooks/cursor-hooks.json` empty: an install fails when a hook reaches Cursor both natively and through its import of Claude Code settings.
3. Write the hook as a Babashka script, `.apm/hooks/scripts/<name>.bb`, started by a bash launcher, `.apm/hooks/scripts/<name>.sh`. When `bb` is missing, the launcher prints that the hook is inactive to standard error and exits 0. Reference the launcher as `${PLUGIN_ROOT}/.apm/hooks/scripts/<name>.sh`. The `.apm/` segment is required, or APM logs "Hook script not found". Keep the launcher executable.
4. Read every input shape and answer in the shape each runtime reads. Claude Code and Codex send `tool_name` and `tool_input` and read `hookSpecificOutput.additionalContext`, and Codex rejects output that also carries a top-level `additionalContext`. Copilot's own copy sends `toolName` and `toolArgs`, where `toolArgs` can be a JSON string or, for `apply_patch`, the patch text, and it reads a top-level `additionalContext`. Copilot's run of the Claude Code copy sends `tool_result`.
5. Add cases to the suites in `tests/hooks/` and run them (see "Hook tests" below).
6. Update `README.md`, increment the `version` field in `apm.yml`, and add a `CHANGELOG.md` entry under a `## [x.y.z] - YYYY-MM-DD` heading for that version.

## Changing an Iron Law

The rules hook delivers `.apm/hooks/scripts/clj-rules.md`, which condenses each Iron Law in `.apm/skills/clojure/SKILL.md` into one `- CLJ-<n>: ...` line, plus the data-driven design rule. When a law or Key Rule 19 changes, update its line in the same change, keeping the law's exceptions. The coordinator's drift check compares the law IDs in both files but cannot compare their meaning.

## Validation

Configuration files:

```bash
python3 -m json.tool opencode.jsonc >/dev/null
python3 -c "import yaml, pathlib; yaml.safe_load(pathlib.Path('apm.yml').read_text())"
python3 -c "import yaml, pathlib; [yaml.safe_load(p.read_text()) for p in pathlib.Path('.apm/skills').glob('*/agents/openai.yaml')]"
```

Runtime install (requires `apm` and the runtime CLIs you want to verify: `claude`, `codex`, `copilot`, `gemini`, `opencode`):

- Run `apm install --target all`, `apm update`, and `apm uninstall` in a clean temporary project. Pre-create the runtime roots: `.agents/`, `.claude/`, `.cursor/`, `.opencode/`, `.gemini/`, `.github/`, `.windsurf/`. With several runtime roots present, APM stops with "Multiple harnesses detected" unless the install names a target. `apm update` reuses the targets that the install records in the project's `apm.yml`.
- Exercise user scope with `apm install brackendev/clojure-skills -g [--target ...]` and `apm uninstall brackendev/clojure-skills -g`. A local filesystem path (`apm install /absolute/path -g`) is also accepted.
- Confirm OpenCode deployment from the temporary project with `opencode api GET "/api/skill?location[directory]=$PWD"`, which lists the skills OpenCode discovers and their source paths. The first request for a directory can return an empty list while OpenCode loads the skills, so repeat it before reading the result.

### Hook tests

Run every hook suite with `bb tests/hooks/run.bb`. It needs `bb`, `git`, `apm`, and `clj-kondo`, and a missing requirement fails the run. `edit-lint-hook.bb` runs the hook against temporary projects with each runtime's input, and `install-check.bb` installs the package with APM at project and user scope, with `--target claude` and with `--target all`, and checks the deployed hook. `clj-rules-hook.bb` checks when `clj-rules` delivers the rules: at session start, on the first matching edit, after a clear or compaction, and never twice or outside a Clojure-family project.

## Skill conventions

For the argument grammar, scope vocabulary, and mutation defaults that every user-invocable skill follows, see [CONVENTIONS.md](CONVENTIONS.md).

| Setting | When to use |
|---------|-------------|
| `user-invocable: true`, `disable-model-invocation: true` | User-only slash command, for any skill whose changes reach beyond the working tree or are hard to undo. |
| `user-invocable: true`, no `disable-model-invocation` | Slash command the model can also run from a natural-language request, for a fixer whose edits stay in the working tree (for example `clj-fix`, `clj-smells-fix`). Write the description as a trigger. |
| `user-invocable: false` | Model-invoked from conversation context (for example `clojure`, `clojure-repl`). |

Every skill carries `agents/openai.yaml` whose `policy.allow_implicit_invocation` matches the table above: `false` for a user-only skill and `true` for every skill the model can run. Skills in this package use the Clojure logo blue brand color, `#5881D8`. The companion packages use distinguishable colors so runtime UIs can tell host-specific guidance apart: Java orange (`#E76F00`) for `clojure-jvm-skills`, JavaScript yellow (`#F7DF1E`) for `clojurescript-skills`, Biff indigo (`#4338CA`) for `biff-skills`, Hyperfiddle lavender (`#B7A4C2`) for `electric-skills`, Fulcro teal (`#009E9E`) for `fulcro-skills`, and Flutter blue (`#02569B`) for `clojuredart-skills`.
