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

;; bad: NPE risk if m is nil
(m :name)
```

### contains? checks keys (Iron Law CLJ-5)

The `contains?` docstring: "for numerically indexed collections like vectors and Java arrays, this tests if the numeric key is within the range of indexes. 'contains?' operates constant or logarithmic time; it will not perform a linear search for a value."

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
;; bad: keeps only some of the entries
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

Floating-point numbers cannot represent most decimal fractions exactly, and clojure.org states that "any operation involving a double or float will result in a double":

```clojure
(+ 0.1 0.2)     ; => 0.30000000000000004
(+ 0.1M 0.2M)   ; => 0.3M, a BigDecimal on the JVM
(+ 0.1M 0.2)    ; => 0.30000000000000004, one double turns the result into a double
(/ 1M 3M)       ; throws "Non-terminating decimal expansion"
(with-precision 10 :rounding HALF_EVEN (/ 1M 3M))  ; => 0.3333333333M
```

On the JVM, keep money in `BigDecimal` from parsing to storage, and choose the rounding mode for every division. ClojureScript has no `BigDecimal` literal, and JavaScript numbers are doubles, so store integer minor units such as cents and keep them below `Number.MAX_SAFE_INTEGER` (9007199254740991). ClojureDart integers are 64-bit on native targets but doubles on the web, so the same limit applies there. Format minor units only for display.

### Core functions from Clojure 1.11

Clojure 1.11 added these functions to `clojure.core`, and ClojureScript and ClojureDart provide them as well (checked against ClojureScript 1.12.145 and ClojureDart 0.9.20261002). Prefer them to reductions written by hand and to host parsing calls:

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
(Long/parseLong "42")                     ; host interop that throws on bad input
```

The `parse-*` functions return `nil` for a string they cannot parse, which suits parsing at a boundary: check for `nil` instead of catching a host exception. They throw when the argument is not a string.

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

A lazy sequence is realized when it is read, not when it is created. When it leaves the scope that it depends on, it is realized after that scope has closed:

```clojure
;; bad: line-seq is realized after with-open closes the reader, and throws "Stream closed"
(defn read-lines [path]
  (with-open [r (io/reader path)]
    (line-seq r)))

;; good: realize the lines while the reader is open
(defn read-lines [path]
  (with-open [r (io/reader path)]
    (doall (line-seq r))))
```

The same applies to `binding` (the dynamic value is gone when the sequence is read) and to `try` (an exception thrown while realizing the sequence escapes the handler).

## Strings

Prefer `clojure.string` functions over host string interop:

```clojure
;; good
(clojure.string/upper-case "bruce")
```

The host skill covers the host's own string namespace where it adds anything beyond `clojure.string`.
