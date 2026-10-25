# Collections, Laziness, and Strings

Part of the `clojure` skill. This host-neutral guidance applies to `.clj`, `.cljs`, `.cljc`, and `.cljd` code.

## Collections

### Keywords as functions

```clojure
(def m {:name "Bruce" :age 30})

;; good
(:name m)

;; bad: verbose
(get m :name)

;; bad: throws when m is nil
(m :name)
```

### contains? checks keys (Iron Law CLJ-5)

The `contains?` docstring on every host says that "for numerically indexed collections like vectors" it "tests if the numeric key is within the range of indexes" and that "it will not perform a linear search for a value."

```clojure
(contains? [:a :b] :a)  ; => false, :a is not an index
(contains? [:a :b] 1)   ; => true, index 1 exists

;; good: membership in a set, or a linear search with some
(contains? #{:a :b} :a)
(some #{:a} [:a :b])
```

### Transients (Iron Law CLJ-4)

clojure.org: transients "are not designed to be bashed in-place. You must capture and use the return value in the next call."

```clojure
;; bad: keeps only some of the entries (8 of 20 on JVM Clojure and ClojureScript)
(let [t (transient {})]
  (dotimes [i 20] (assoc! t i i))
  (persistent! t))

;; good: thread the return value through every call
(persistent! (reduce (fn [t i] (assoc! t i i)) (transient {}) (range 20)))
```

### Sets as predicates

```clojure
;; good
(remove #{1} [0 1 2 3 4 5])
(count (filter #{\a \e \i \o \u} "mary had a little lamb"))

;; bad
(remove #(= % 1) [0 1 2 3 4 5])
```

### seq for empty checks

Use `seq` for nil punning instead of `empty?`:

```clojure
;; good
(when (seq s)
  (prn (first s)))

;; bad
(when-not (empty? s)
  (prn (first s)))
```

### Prefer vectors

```clojure
;; good
[1 2 3]

;; bad: quoted list as container
'(1 2 3)
```

### Keyword keys

```clojure
;; good
{:name "Bruce" :age 30}

;; bad
{"name" "Bruce" "age" 30}
```

### Namespaced keys

Use namespaced keywords to signal parsed domain data and prevent key collisions:

```clojure
;; good: namespaced keys mark domain-specific data
{:order/id 123
 :order/total 45.99
 :order/status :pending}

;; bad: bare keys collide across domains
{:id 123
 :total 45.99
 :status :pending}
```

Use a schema library available on the target runtime (`clojure.spec.alpha` and Malli on JVM and ClojureScript) at system boundaries to parse raw input into domain maps. Downstream functions receive conformed data and do not re-validate. The host skill names the runtime-specific options.

### Commas in collections

No commas in sequential collection literals (vectors, lists). Commas in maps are optional for readability:

```clojure
;; good
[1 2 3]
{:name "Bruce" :age 30}
{:name "Bruce", :age 30}  ;; also good in maps

;; bad
[1, 2, 3]
```

### vec over into

```clojure
;; good
(vec some-seq)

;; bad
(into [] some-seq)
```

### list* over nested cons

```clojure
;; good
(list* 1 2 3 [4 5])

;; bad
(cons 1 (cons 2 (cons 3 [4 5])))
```

### Prefer destructuring over index access

```clojure
;; good
(let [[x y] point]
  (process x y))

;; bad: fragile, says nothing about what index 0 and 1 mean
(process (nth point 0) (nth point 1))
```

### Record constructors

Use auto-generated factory functions, not interop syntax:

```clojure
(defrecord Foo [a b])

;; good
(->Foo 1 2)
(map->Foo {:a 3 :b 4})

;; bad: interop form
(Foo. 1 2)
```

The factory functions exist on every host that ships `defrecord` (JVM Clojure, ClojureScript, ClojureDart). ClojureDart raises the stakes further: the raw interop form requires three extra arguments (`nil {} -1` for meta, extmap, hash) that the factory functions supply automatically.

### Numeric idioms

```clojure
;; good
(inc x)
(dec x)
(pos? x)
(neg? x)
(zero? x)

;; bad
(+ x 1)
(- x 1)
(> x 0)
(< x 0)
(= x 0)
```

### Money (Iron Law CLJ-9)

Floating-point numbers cannot represent most decimal fractions exactly, on every host:

```clojure
(+ 0.1 0.2)     ; => 0.30000000000000004
```

Keep money in an exact decimal type from parsing to storage where the host has one, and never mix it with floating-point values. Where the host has no decimal type, store integer minor units such as cents and format them only for display.

- **JVM Clojure** has `BigDecimal`. `clojure-jvm/references/numbers.md` in the `clojure-jvm` skill (clojure-jvm-skills) covers `M` literals, the double contagion of mixed arithmetic, division with an explicit rounding mode, and parsing money at a boundary.
- **ClojureScript** has no decimal type and reads `0.1M` as an ordinary JavaScript number, so the `M` suffix gives no decimal precision there. JavaScript numbers are doubles, so keep minor units below `Number.MAX_SAFE_INTEGER` (9007199254740991). `clojurescript/references/host-semantics.md` in the `clojurescript` skill (clojurescript-skills) covers JavaScript numbers and money.
- **ClojureDart** has no decimal type in its core library. It compiles `0.1M` to a Dart double, so `(+ 0.1M 0.2M)` is `0.30000000000000004` there, and `cljd.edn/read-string` throws "BigDecimal not supported yet." on `"0.1M"`. Its integers are 64-bit on native targets but doubles on the web, so the `Number.MAX_SAFE_INTEGER` limit applies on the web. `clojuredart/references/errors-and-resources.md` in the `clojuredart` skill (clojuredart-skills) covers `M`, `N`, and ratio literals.

### Core functions for maps, parsing, and UUIDs

These `clojure.core` functions exist in JVM Clojure, ClojureScript, and ClojureDart (checked against ClojureScript 1.12.145 and ClojureDart 0.9.20261002). Prefer them to reductions written by hand and to host parsing calls:

```clojure
;; good
(update-vals prices inc)                  ; apply f to every value
(update-keys row keyword)                 ; apply f to every key
(parse-long "42")                         ; => 42, or nil when the string is not an integer
(parse-double "4.2")                      ; => 4.2, or nil
(parse-boolean "true")                    ; => true; only "true" and "false" parse, anything else is nil
(parse-uuid "4fe9...")                    ; => a UUID, or nil
(random-uuid)
(abs -3)                                  ; => 3

;; bad
(into {} (map (fn [[k v]] [k (inc v)])) prices)
(reduce-kv (fn [m k v] (assoc m (keyword k) v)) {} row)
(Long/parseLong "42")                     ; a JVM host call, which throws on bad input
```

The `parse-*` functions return `nil` for a string they cannot parse, which suits parsing at a boundary: check for `nil` instead of catching a host exception. They throw when the argument is not a string. The hosts accept different edge cases: ClojureDart's `parse-long` calls Dart's `int.tryParse`, which accepts surrounding whitespace and a `0x` hexadecimal prefix, so `(parse-long " 42")` returns `42` there and `nil` on the JVM and in ClojureScript. When the accepted format matters, match the string against a regular expression before parsing it.

## Laziness

Clojure sequences are lazy by default. `map`, `filter`, `remove`, and `take` return lazy sequences. When side effects matter, unrealized lazy sequences are a source of bugs.

```clojure
;; bad: map returns a lazy seq, side effects may never execute
(map println ["a" "b" "c"])

;; good: run! forces side effects, returns nil
(run! println ["a" "b" "c"])

;; good: doseq for side-effecting iteration
(doseq [x ["a" "b" "c"]]
  (println x))

;; good: mapv returns an eager vector
(mapv process items)

;; good: doall forces a lazy seq when you need the realized collection
(doall (map transform items))
```

When building a result, prefer eager functions (`mapv`, `filterv`, `reduce`) or transducers. When composing transformations for later consumption, lazy sequences are appropriate.

### Lazy sequences and scope (Iron Law CLJ-2)

A lazy sequence is realized when it is read, not when it is created. When it leaves the scope that it depends on, it is realized after that scope has closed. These examples behave the same on the JVM, in ClojureScript, and in ClojureDart:

```clojure
(def ^:dynamic *rate* 1)

(defn parse-all [inputs]
  (map parse-one inputs))   ; lazy: parse-one runs when the result is read

;; bad: the sequence is read after binding ends, so it uses the root value: (1 2 3)
(binding [*rate* 10]
  (map #(* % *rate*) [1 2 3]))

;; good: realize it while the binding is in effect: [10 20 30]
(binding [*rate* 10]
  (mapv #(* % *rate*) [1 2 3]))

;; bad: parse-all returns a lazy sequence, so its exception is thrown after try returns
(try
  (parse-all inputs)
  (catch #?(:cljd Object :cljs :default :clj Exception) _ :invalid))

;; good: the exception is thrown inside try
(try
  (doall (parse-all inputs))
  (catch #?(:cljd Object :cljs :default :clj Exception) _ :invalid))
```

The same applies to `with-open`, which closes the resource before a lazy result is read. `clojure-jvm/references/errors-and-resources.md` in the `clojure-jvm` skill (clojure-jvm-skills) shows the JVM reader case, which fails with "Stream closed", and `clojuredart/references/errors-and-resources.md` in the `clojuredart` skill (clojuredart-skills) covers ClojureDart's `with-open`. ClojureScript has no `with-open`. Release resources there in `try`/`finally`, as `clojurescript/references/async-and-errors.md` in the `clojurescript` skill (clojurescript-skills) describes. `references/reader-conditionals.md` explains the catch-all types in the `try` example.

## Strings

Prefer `clojure.string` functions over host string interop:

```clojure
;; good
(clojure.string/upper-case "bruce")
```

The host skill covers the host's own string namespace where it adds anything beyond `clojure.string`.
