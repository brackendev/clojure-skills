---
name: clojure-deps
description: >-
  Use when choosing, checking, or researching a dependency of a Clojure,
  ClojureScript, or ClojureDart project: which version the project uses, what
  the latest release is, whether a newer version is compatible, or how a
  library's API works in the installed version. Triggers: `deps.edn` `:deps`
  and `:extra-deps` coordinates, `:mvn/version`, `:git/tag`, `:git/sha`,
  `project.clj` `:dependencies`, `shadow-cljs.edn` `:dependencies`, Clojars,
  Maven Central, cljdoc, `find-versions`, "latest version of", "which version",
  "is this outdated", "add a library", or "how do I use" a Clojure library.
  Covers reading the resolved version, finding source and documentation for
  that exact version, looking up releases, and separating the latest release
  from a compatible one.
user-invocable: false
---

# Clojure Dependencies

This skill covers researching a dependency before using or changing it. It reports versions and sources. Changing a dependency declaration remains a separate, deliberate edit, and some packages provide an upgrade command for their framework (`/cljs-upgrade`, `/fulcro-upgrade`, `/cljd-upgrade`).

## Key Rules

1. **Start from the version the project resolves.** The declared coordinate is where to start, but the classpath can hold a different version when another dependency pulls one in. Check the resolved version before reading documentation or source.
2. **Read documentation and source for that exact version.** An API can change between releases. Use versioned documentation (cljdoc pages carry the version in the URL) or the source inside the resolved jar. Do not rely on memory or on the newest documentation.
3. **Look up newer releases only when the task needs them.** A question about how a library works does not need a network lookup. A question about upgrading or about the latest version does.
4. **Keep "latest" and "compatible" separate.** The newest release can be a prerelease, can need a newer Java or ClojureScript version, or can change an API the project uses. Report the latest stable release, the latest prerelease when it is newer, and what compatibility you checked.
5. **Report sources and gaps.** Name the command or URL behind each version claim. A failed lookup means the answer is unknown, not that the library has no newer release.

## Finding the resolved version

| Build tool | Command |
|------------|---------|
| Clojure CLI (`deps.edn`) | `clojure -X:deps tree` prints the resolved dependency tree. Add `:aliases '[:test]'` to include an alias. |
| Leiningen (`project.clj`) | `lein deps :tree` |
| shadow-cljs | With `:deps` in `shadow-cljs.edn`, the Clojure CLI resolves the dependencies, so use `clojure -X:deps tree` with the aliases it names. npm packages resolve through `package.json`: `npm ls <package>`. |
| ClojureDart | The `tensegritics/clojuredart` entry in `deps.edn` is a Git coordinate. Its `:tag` and `:sha` name the version. |

To read a library's source for the resolved version, find its jar on the classpath (`clojure -Spath | tr ':' '\n' | grep <artifact>`) and list or print files from it with `unzip -l` and `unzip -p`. In a running JVM REPL, `(clojure.repl/source fn-name)` and `(clojure.repl/doc fn-name)` show the loaded version.

## Looking up releases

| Source | Command or URL | Notes |
|--------|----------------|-------|
| Clojure CLI | `clojure -X:deps find-versions :lib group/artifact` | Lists Maven versions, or Git tags with their SHA for a library whose Git URL the CLI infers from its name, such as an `io.github` library. It prints the newest 8 in ascending order, prereleases included. `:n 5` changes the count, and `:n :all` prints every version. |
| Clojars | `curl -sSL -H 'Accept: application/json' https://clojars.org/api/artifacts/<group>/<artifact>` | `latest_release` can be a prerelease, for example an `-alpha` version, and `latest_version` can be a `-SNAPSHOT`. Read `recent_versions` to find the latest stable release. |
| Maven Central | `https://repo1.maven.org/maven2/<group path>/<artifact>/maven-metadata.xml` | `<release>` and `<latest>` can name a prerelease, for example an `-alpha` version newer than the latest stable release. Read the `<versions>` list. |
| Git dependencies | `git ls-remote --tags <url>` | Pin both `:git/tag` and `:git/sha`. Use the commit SHA: for an annotated tag, the line that ends in `^{}`. |

## Reading documentation

- [cljdoc](https://cljdoc.org) hosts documentation for Clojars and Maven libraries at `https://cljdoc.org/d/<group>/<artifact>/<version>`. Without a version, the URL redirects to the latest one, so always include the resolved version.
- The library's own repository holds its README and `CHANGELOG.md`. Read the changelog entries between the resolved version and the candidate version before recommending an upgrade.
- When the `context7` skill from project-skills is installed, its documentation lookup can supplement these sources. Check that what it returns matches the resolved version.

## Host and framework notes

- **ClojureScript.** shadow-cljs and figwheel-main projects also depend on npm packages. `npm view <package> version` shows the latest release. The `clojurescript` skill covers npm interop.
- **ClojureDart.** Releases are GitHub releases of `Tensegritics/ClojureDart`, and `/cljd-upgrade` performs the upgrade. Dart packages come from `flutter pub add` and `pubspec.yaml`, as the `clojuredart` skill describes.
- **Frameworks.** Biff 2, Electric v3, and Fulcro 3 skills apply only to those major versions. Check the major version before applying their guidance to a project.
