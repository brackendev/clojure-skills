# Threading, Bindings, and Control Flow

Part of the `clojure` skill. This host-neutral guidance applies to `.clj`, `.cljs`, `.cljc`, and `.cljd` code.

## Threading Macros

Use `->` when the argument threads into the first position. Use `->>` when it threads into the last position:

```clojure
;; -> thread-first
(-> [1 2 3]
    reverse
    (conj 4)
    prn)

;; ->> thread-last
(->> (range 1 10)
     (filter even?)
     (map #(* 2 %)))
```

Omit parentheses around forms that take no extra arguments:

```clojure
;; good
(-> x fizz :foo first frob)

;; bad: unnecessary parens
(-> x (fizz) (:foo) (first) (frob))
```

### Conditional Threading

`cond->` and `cond->>` apply transformations only when conditions are true:

```clojure
(cond-> m
  name    (assoc :name name)
  age     (assoc :age age)
  admin?  (assoc :role :admin))
```

### Nil-safe Threading

`some->` and `some->>` short-circuit on `nil`. Use them when intermediate steps may return `nil`:

```clojure
;; good: stops if any step returns nil
(some-> user :address :city str/upper-case)

;; bad: NPE risk if :address is nil
(-> user :address :city str/upper-case)
```

`if-some` and `when-some` bind non-nil values (unlike `if-let`/`when-let`, which test for logical truth and treat `false` as failure):

```clojure
;; good: handles false correctly
(when-some [v (get config :enabled)]
  (println "enabled:" v))

;; bad: treats false as missing
(when-let [v (get config :enabled)]
  (println "enabled:" v))
```

## Variable Binding

Minimize `let` bindings. Inline values used only once:

```clojure
;; good: inline single-use value
(str/upper-case (get-name user))

;; bad: unnecessary binding
(let [name (get-name user)]
  (str/upper-case name))
```

Use threading macros to eliminate intermediate bindings:

```clojure
;; good
(->> users
     (filter active?)
     (map :name)
     sort)

;; bad
(let [active (filter active? users)
      names  (map :name active)]
  (sort names))
```

Use destructuring in function parameters instead of separate `let` bindings:

```clojure
;; good
(defn process [{:keys [name age]}]
  (str name " is " age))

;; bad
(defn process [person]
  (let [name (:name person)
        age  (:age person)]
    (str name " is " age)))
```

## Control Flow

Track actual values instead of boolean flags:

```clojure
;; good: return the found value or nil
(defn find-user [id users]
  (first (filter #(= id (:id %)) users)))

;; bad: boolean flag with separate lookup
(defn user-exists? [id users]
  (some #(= id (:id %)) users))
```

Return `nil` for "not found" conditions rather than wrapper objects:

```clojure
;; good
(when-let [user (find-user id)]
  (process user))

;; bad
(let [result (find-user id)]
  (if (:found? result)
    (process (:user result))
    (handle-missing)))
```

## Exception Handling

Prefer `ex-info` for data-carrying exceptions across every host:

```clojure
;; good
(throw (ex-info "Invalid input" {:value x :reason :negative}))
```

When rethrowing, pass the caught exception as the third argument so the root cause stays in the chain (Key Rule 17 in `SKILL.md`):

```clojure
;; good: the cause survives
(try
  (parse-order input)
  (catch Exception e  ; host-specific catch type, see the host skill
    (throw (ex-info "Order import failed" {:input input} e))))
```

Host skills cover host-typed exceptions, resource cleanup (`with-open` on the JVM), and the catch-all rules (no `Throwable` on the JVM, `:default` in ClojureScript, Dart `Exception` in ClojureDart).
