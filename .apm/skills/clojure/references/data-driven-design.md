# Data-Driven Design

Part of the `clojure` skill. This host-neutral guidance applies to `.clj`, `.cljs`, `.cljc`, and `.cljd` code. It expands Key Rule 19: when a catalog, registry, or configuration map describes the things a program works with, the data owns the policy and the code stays generic.

Two parts of the rule come from the Honest Code Principles: P15 (No Implicit Defaults) and P16 (Dispatch Tables Close Open Input) reject a default that answers for an input nobody declared, and require a lookup of an unknown key to fail. The rest, declaring relationships between catalog entries and answering each question with one function, is this package's rule.

## Where the rule applies

The rule applies when a program keeps a collection of entries that share a shape: tools, data sources, feature flags, plugins, report columns, deployment targets. Each entry is a map, and the program decides what to do by reading those maps.

The rule does not replace ordinary dispatch. A `case` on a fixed set of variants that belong to the code, such as `:circle` and `:rect` in `references/state-and-dispatch.md`, is fine. The smell is code that branches on the name of a catalog entry, so adding, removing, or renaming an entry needs a code change as well as a data change.

## Declare relationships in the data

Each entry states what it provides and what it needs. Relationships between entries exist only as these declarations.

```clojure
(def tools
  [{:tool/id :gh       :tool/provides #{:pull-requests :issues}}
   {:tool/id :jira-cli :tool/provides #{:issues}}
   {:tool/id :docker   :tool/provides #{:containers}}])

(def sources
  [{:source/id :github-prs  :source/requires #{:pull-requests}}
   {:source/id :jira        :source/requires #{:issues}}
   {:source/id :compose     :source/requires #{:containers}}])
```

## Answer each question with one function

Write one pure function per question. Every caller, such as the CLI, the web page, a health check, or a test, asks that function. No caller derives its own answer.

```clojure
(defn providers
  "The tools that provide capability."
  [tools capability]
  (filter #(contains? (:tool/provides %) capability) tools))

(defn source-available?
  "True when every capability the source requires has an installed provider."
  [tools installed-ids source]
  (let [installed (filter #(installed-ids (:tool/id %)) tools)]
    (every? #(seq (providers installed %)) (:source/requires source))))
```

```clojure
;; bad: the code names entries, so the catalog no longer decides
(defn source-available? [installed-ids source]
  (case (:source/id source)
    :github-prs (contains? installed-ids :gh)
    :jira       (or (contains? installed-ids :jira-cli) (contains? installed-ids :gh))
    true))
```

The bad version has three defects that the data-driven version cannot have. It duplicates the catalog, so a new tool that provides `:issues` changes nothing until someone edits the `case`. It invents a relationship (`:gh` for Jira) that no entry declares. Its default branch, `true`, turns an unknown source into an available one instead of reporting that nothing declares how to provide it.

## No declaration means no behavior

When no entry declares a relationship, the answer is "none," not a guess. A default that the code supplies is a relationship that the data never stated, and it survives every later change to the data (Honest Code P15 and P16).

```clojure
;; bad: a hardcoded fallback for sources without :source/requires
(defn required-capabilities [source]
  (or (:source/requires source) #{:pull-requests}))

;; bad: a quieter default; a source that forgot the key looks like one that needs nothing
(defn required-capabilities [source]
  (:source/requires source #{}))

;; good: every source declares the key, and a source that needs nothing declares #{}
(defn required-capabilities [source]
  (:source/requires source))
```

Make absence explicit in the data. The load-time check requires `:source/requires` on every source, so "requires nothing" is a declared `#{}` and a missing key is a rejected entry that the resolver never sees.

## Validate the data when it loads

Check the catalog once, at the boundary where it enters the program, and fail with the offending entry in `ex-data`. Downstream functions then trust the shape and do not re-check it (`references/collections.md` covers parsing at boundaries).

Check what the resolvers depend on: required keys, value types, unique ids, and references that point at nothing.

```clojure
(defn validate-catalog!
  "Returns the catalog when every source declares :source/requires and every
  required capability has a declared provider. Throws ex-info naming the
  undeclared sources or the unprovided capabilities otherwise."
  [{:keys [tools sources] :as catalog}]
  (let [undeclared (vec (keep #(when-not (contains? % :source/requires) (:source/id %)) sources))
        provided (into #{} (mapcat :tool/provides) tools)
        missing (for [source sources
                      capability (:source/requires source)
                      :when (not (provided capability))]
                  {:source/id (:source/id source) :capability capability})]
    (when (seq undeclared)
      (throw (ex-info "Sources must declare :source/requires, using #{} for none"
                      {:undeclared undeclared})))
    (when (seq missing)
      (throw (ex-info "Catalog declares requirements that no tool provides"
                      {:missing (vec missing)})))
    catalog))
```

On the JVM and in ClojureScript, `clojure.spec.alpha` or Malli can describe the entry shapes, and the cross-entry checks stay in a function like the one above. ClojureDart has neither library, so write the whole check as a function that returns the catalog or throws `ex-info`.

## Keep the code small and generic

- Keep the catalog as plain maps and sets, read from EDN or defined in one namespace. Do not wrap entries in records or protocols unless a host type requires it.
- Name capabilities and ids with qualified keywords, so a search for `:tool/provides` finds every declaration and every reader.
- Put effects at the edges. The resolvers take the catalog and the observed facts (installed tools, environment) as arguments, so tests call them with literal maps.
- When a new kind of question appears, add one function next to the others. Do not answer it inside a handler or a view.

## Review checklist

- Does any `case`, `cond`, `condp`, or `=` test compare against a specific catalog id? Move the decision into an entry's declarations.
- Does any function supply a default for a relationship the data does not declare? Remove the default and decide the absent case in the schema.
- Do two places compute the same answer? Keep one function and call it from both.
- Does the catalog load without a check? Add one at the boundary.
