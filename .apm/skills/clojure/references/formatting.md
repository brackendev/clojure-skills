# Formatting and Docstrings

Part of the `clojure` skill. This host-neutral guidance applies to `.clj`, `.cljs`, `.cljc`, and `.cljd` code.

## Formatting

- 2-space indentation, no tabs
- Aim for 80 characters per line (120 maximum)
- Vertically align `let` bindings and map keys
- Single blank line between top-level forms
- No blank lines within function or macro definitions, except a blank line between `let` or `cond` pairs that do not fit on one line
- Gather closing parentheses on the same line

```clojure
;; good
(defn process [items]
  (let [filtered (filter valid? items)
        mapped   (map transform filtered)]
    (reduce combine mapped)))

;; bad: closing parens on separate lines
(defn process [items]
  (let [filtered (filter valid? items)
        mapped   (map transform filtered)]
    (reduce combine mapped)
  )
)
```

### Comments

- `;;;;` for section headings
- `;;;` for top-level comments outside definitions
- `;;` for code fragment comments (before the line they describe)
- `;` for margin (inline) comments

Use `#_` to comment out forms instead of `;`:

```clojure
;; good
(+ foo #_(bar x) delta)

;; bad
(+ foo
   ;; (bar x)
   delta)
```

## Docstrings

Place docstrings after the function name, not after the argument vector. Start with a complete, capitalized sentence on the first line. Wrap parameter references in backticks:

```clojure
;; good
(defn frobnitz
  "Computes the frobnitz of `x`.
  Returns nil when `x` is negative."
  [x]
  ...)

;; bad: docstring after arg vector (becomes a string in the body)
(defn frobnitz [x]
  "Computes the frobnitz of x."
  ...)
```
