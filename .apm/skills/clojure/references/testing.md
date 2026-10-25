# Testing

Part of the `clojure` skill. This host-neutral guidance applies to `clojure.test`, `cljs.test`, and `cljd.test`. Host skills cover each test runner.

## Testing

Name test namespaces `yourproject.something-test`. Name tests `something-test`:

```clojure
;; good
(deftest user-validation-test ...)

;; bad
(deftest test-user-validation ...)
(deftest user-validation-tests ...)
```

Use `testing` blocks for context in failure output:

```clojure
(deftest user-validation-test
  (testing "rejects blank names"
    (is (not (valid? {:name ""}))))
  (testing "accepts valid names"
    (is (valid? {:name "Bruce"}))))
```

Use `are` for tabular tests:

```clojure
(deftest palindrome?-test
  (are [s expected] (= expected (palindrome? s))
    "racecar" true
    "hello"   false
    "madam"   true
    ""        true))
```

`thrown?` and `thrown-with-msg?` accept host-specific exception types. Examples and host-specific guidance (JVM types, ClojureScript `:default`, ClojureDart `Exception`) live in the host skill.

Prefer passing dependencies as function arguments. `with-redefs` exists on the JVM and in ClojureScript but not in ClojureDart; the JVM and ClojureScript skills cover the trade-offs there.
