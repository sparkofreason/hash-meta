# hash-meta

[![Clojars Project](https://img.shields.io/clojars/v/sparkofreason/hash-meta.svg)](https://clojars.org/sparkofreason/hash-meta)

## tl;dr

Provides macros to abuse Clojure(Script) reader tags. The main anticipated
use-case is for customized debugging. Why reader tags? They are
minimally invasive in your code, and easily removed by find/replace
or rendered no-ops by editing `data_readers.clj`.
Examples:

```clojure
;;; We want to debug this expression
(inc (foo 2 (bar 3 (foo 4 5))))

;;; Do you want to rewrite like this:
(let [r1 (foo 4 5)
      _ (println "(foo 4 5)" r1)
      r2 (bar 3 r1)
      _ (println "(bar 3 r1)" r2)
      r3 (foo 2 r2)
      _ (println "(foo 2 r2") r3)
      r4 (inc r3)]
    (println "(inc r3)" r4)
    r4)
    
;;; Or this?
(inc #pp (foo 2 #pp (bar 3 #pp (foo 4 5))))
```

Example `#pp` definition:

```clojure
(ns hashpp
  (:require [sparkofreason.hash-meta.core :as ht :refer [defreader-n]]))

(defreader-n pp
  (fn [executable-form readable-form _]
    `(let [r# ~executable-form]
       (println '~readable-form "=>" r#)
       r#)))

user=> (inc #pp (foo 2 #pp (bar 3 #pp (foo 4 5))))
(foo 4 5) => 20
(bar 3 (foo 4 5)) => 23
(foo 2 (bar 3 (foo 4 5))) => 46
=> 47
```

You can also just mangle code, macro-style:

```clojure
(ns infix.clj
  (:require [sparkofreason.hash-meta.core :as ht :refer [defreader]]))

(defreader i 
  (fn [f _]
    (let [[v1 op v2] f]
      `(~op ~v1 ~v2))))

#i (2 + 3)
=> 5
```

`defreader` is just a short-cut for defining a data-reader macro. 
`defreader-n` does a little more work, with the idea that you may want
access both to the executable form and a more readable pre-macroexpansion
version (discussed further below).

Both `defreader-n` and `defreader` will modify the thread-local binding of
`clojure.core/*data-readers*` to include your reader tag definition. This
makes for easy hacking in the REPL, since you can define and use the tag
in the same namespace as shown above, without having to add it to your
`data_readers.clj(c)` file. To use with ClojureScript at all, or to use
your reader tag in the general case in Clojure code,
you will need to modify `data_readers.clj(c)`.

hash-meta is best used when you need macro-level functionality to define
custom reader macros. If you just want customized processing of debug
data via your own functions, the [hashtag][] library will be easier to
use.

## Usage

### `defreader`

Example:

```clojure
(defreader i 
  (fn [f m]
    (let [[v1 op v2] f]
      `(~op ~v1 ~v2))))
```

`defreader` takes two arguments:

* `id` - any valid unnamespaced symbol
* `transform` - a function of two arguments. The first argument will be the tagged 
form, and the second any metadata associated witht the form. The return value 
should be a valid Clojure s-expression, as with any macro.

### `defreader-n`

Example:

```clojure
(defreader-n t
  (fn [f f' m]
    `(let [r# ~f]
       (println '~f' "=>" r# "<" (:t ~m "") ">")
       r#)))
```
 
`defreader-n` takes two arguments:
 
* `id` - any valid unnamespaced symbol
* `transform` - a function of three arguments. The first argument is the form
as seen by the compiler, and will include the results of macroexpansion of
any nested hashtagged forms. The second argument is a "readable" version, the
best guess of the unmangled form, before macroexpansion and minus any nested
hashtags. The third argument contains any metadata associated with the form.
The return value should be a valid Clojure s-expression, as with any macro.

### Using a reader tag

hash-meta definitions can be used with in general with CLJ/CLJS/CLJC code. 
Define the reader macro in a clj file (or via reader-conditional in cljc):

```clojure
;;; hashpp.clj
(ns hashpp
  (:require [sparkofreason.hash-meta.core :as ht :refer [defreader-n]]
            [net.cgrand.macrovich :as macros]))

(defreader-n pp
  (fn [f f']
      `(let [r# ~f]
         (macros/case
          :clj (println '~f' "=>" r#)
          :cljs (.log js/console (str '~f' " => " r#)))
         r#)))
```

Include a `data_readers.cljc` on the classpath:

```clojure
{pp hashpp/pp}
```

Then use the macro as required for the environment:

```clojure
;;; test.cljc
(ns test
  #?(:clj (:require [hashpp])
     :cljs (:require-macros [hashpp])))

(inc #pp (* 2 #pp (+ 3 #pp (* 4 5))))
```
If you make your own library of reader tags, you can
make them generally available by preloading the code
defining the tags via whatever mechanism your tooling
provides. See [this discussion on hashp for examples](https://github.com/weavejester/hashp#install).

## `defreader` vs. `defreader-n`

Example definitions:

```clojure
(ns reader-vs-hash
  (:require [sparkofreason.hash-meta.core :as ht :refer [defreader-n defreader]]
            [clojure.pprint :refer [pprint]]))

(defreader reader-p
  (fn [f]
    `(let [x# ~f]
       (pprint {:result x#
                :form '~f})
       x#)))

(defreader-n hashtag-p
  (fn [f f']
    `(let [x# ~f]
       (pprint {:result x#
                :form '~f'})
       x#)))
```

The output using `#reader-p` looks as follows:

```clojure
user=> (inc #reader-p (* 2 #reader-p (+ 3 #reader-p (* 4 5))))

{:result 20, :form (* 4 5)}
{:result 23,
 :form
 (+
  3
  (clojure.core/let
   [x__3010__auto__ (* 4 5)]
   (clojure.pprint/pprint {:result x__3010__auto__, :form '(* 4 5)})
   x__3010__auto__))}
{:result 46,
 :form
 (*
  2
  (clojure.core/let
   [x__3010__auto__
    (+
     3
     (clojure.core/let
      [x__3010__auto__ (* 4 5)]
      (clojure.pprint/pprint {:result x__3010__auto__, :form '(* 4 5)})
      x__3010__auto__))]
   (clojure.pprint/pprint
    {:result x__3010__auto__,
     :form
     '(+
       3
       (clojure.core/let
        [x__3010__auto__ (* 4 5)]
        (clojure.pprint/pprint
         {:result x__3010__auto__, :form '(* 4 5)})
        x__3010__auto__))})
   x__3010__auto__))}
   
=> 47
```

Reader tags are expanded "inside-out", with the inner-most
s-expression expanded first. So as `#reader-p` expands for
progressively less-nested forms, the output is increasingly
polluted with the macro-expansion, clearly not desirable for
debugging. However, we still need to execute the expanded form
to get the side-effects, which was the whole point of defining
the reader tag in the first place. `defreader-n` facilitates this:

```clojure
user=> (inc #hashtag-p (* 2 #hashtag-p (+ 3 #hashtag-p (* 4 5))))

{:result 20, :form (* 4 5)}
{:result 23, :form (+ 3 (* 4 5))}
{:result 46, :form (* 2 (+ 3 (* 4 5)))}

=> 47
```

hash-meta will deal with different tags in nested
forms, as long as they are defined with `defreader-n`. So this
also works (assuming definitions for `pp` and `p2`):

```clojure
user=> (inc #p2 (* 2 #pp (+ 3 #p2 (* 4 5))))

FOO (* 4 5) 20
{:locals {}, :result 23, :form (+ 3 (* 4 5))}
FOO (* 2 (+ 3 (* 4 5))) 46

=> 47
```

[hashp] handles this for its specific implementation ([spyscope][]
does not, which has blocked me from using it in the past).
hash-meta needed to solve this for the general(ish) case of any
reader macro definition. This is currently handled using
[core.unify][], which is probably about as good as it's 
going to get. The technique is far from bulletproof, and the
rule of thumb to get it to work for you is that your reader
macros need to keep the tagged form and metadata intact in the 
literal output of the macro so it can be recognized
and extracted. If you mangle the form as is done in the `infix` 
example above, unification won't succeed, and you'll get the
macro-expanded output for nested forms. Examples shown below:

```clojure
(defreader-n t
  (fn foo [f f' m]
    `(let [r# ~f]
       (println '~f' "=>" r# "<" (:t ~m "") ">")
       r#)))

user=> (inc #t ^{:t :foo} (* 2 #t (+ 3 #t ^{:t "BAR" :other :metadata} (* 4 5))))
(* 4 5) => 20 < BAR >
(+ 3 (* 4 5)) => 23 <  >
(* 2 (+ 3 (* 4 5))) => 46 < :foo >
=> 47
```

This works as expected, since the forms and metadata passed to
the function are output directly in their literal form. 

```clojure
(defreader-n t
  (fn foo [f f' m]
    `(let [r# ~f]
       (println '~f' "=>" r# "<" ~(:t m "") ">")
       r#)))

user=> (inc #t ^{:t :foo} (* 2 #t (+ 3 #t ^{:t "BAR" :other :metadata} (* 4 5))))
(* 4 5) => 20 < BAR >
(+ 3 (clojure.core/let [r__4895__auto__ (* 4 5)] (clojure.core/println (quote (* 4 5)) => r__4895__auto__ < BAR >) r__4895__auto__)) => 23 <  >
(* 2 (+ 3 (clojure.core/let [r__4895__auto__ (* 4 5)] (clojure.core/println (quote (* 4 5)) => r__4895__auto__ < BAR >) r__4895__auto__))) => 46 < :foo >
=> 47
```

The change here is subtle. The expression to extract the metadata 
attribute `:t` changed from `(:t ~m "")` to `~(:t m "")`, which
ordinarily wouldn't make much difference, but here causes
unification to not recognize the output form as coming from
a registered hashtag.

## Motivation and Uses

hash-meta was forked as a generalization of [hashtag][], which
in turn was forked as a generalization of [hashp][]. I like the
idea of using tagged literals for debugging. They're short to type,
minimally invasive in code, and easily removed by simple
find/replace. I believe [spyscope][] pioneered this idea for
Clojure. [hashp][] put it's own spin on it, in particular dealing
with the nested macroexpansion issue. [hashtag][] generalized
the [hashp][] approach, allowing an arbitrary function to be supplied
to process the debug info, instead of just printing it with some
hard-coded formatting choices.

I would guess that hash-meta will usually be used as the basis
for other libraries providing custom reader macros, as is done
with [hashtag][].

### Macros as reader tags

One obvious shortcoming became apparent while working on [hashtag][], 
in that it would only work with functions. Thus was born hash-meta. 
The Clojure `time` macro provides a simple example.

```clojure
(defreader-n t
  (fn [f _ _]
    `(time ~f)))
    
user=> #t (Thread/sleep 1000)
"Elapsed time: 1003.026079 msecs"
=> nil
```

Many excellent debugging
libraries like [postmortem] and [debux] use macros, and it seemed
like you should be able to adapt them for use with the hashtag syntax
advantages. Here's an example using the `dbgn`
macro from [debux]:

```clojure
(defreader dbgn
  (fn [form m]
    (if-let [args (:args m)]
      `(debux/dbgn ~form ~args)
      `(debux/dbgn ~form))))
 ```
 
 Since `dbgn` is a macro, we have to use hash-meta instead of
 [hashtag][]. We can now use `dbgn` as a reader tag:
 
 ```clojure
 #dbgn ^{:args :dup} (loop [acc 1 n 3]
                      (if (zero? n)
                        acc
                        (recur (* acc n) (dec n))))

{:ns clj.dbgn}
dbgn: (loop [acc 1 n 3] (if (zero? n) acc (recur (* acc n) (dec n)))) =>
 
| n =>
|   3
| (zero? n) =>
|   false
| acc =>
|   1
| n =>
|   3
| (* acc n) =>
|   3
| n =>
|   3
| (dec n) =>
|   2
 
| n =>
|   2
| (zero? n) =>
|   false
| acc =>
|   3
| n =>
|   2
| (* acc n) =>
|   6
| n =>
|   2
| (dec n) =>
|   1
 
| n =>
|   1
| (zero? n) =>
|   false
| acc =>
|   6
| n =>
|   1
| (* acc n) =>
|   6
| n =>
|   1
| (dec n) =>
|   0
 
| n =>
|   0
| (zero? n) =>
|   true
| acc =>
|   6
| (loop [acc 1 n 3] (debux.common.util/insert-blank-line) (if (zero? n)  ... =>
|   6
```

### Debugging in the context of other macros
Another case where macro-fu is required is debugging where
threading macros are used. `->` and `->>` rewrite forms, so
simply wrapping the form in a `let` and adding some output 
will not compile.

```clojure
(defreader-n pp->>
  (fn [f f' _]
    `((fn [x#]
        (let [result# (->> x# ~f)]
          (pprint {:result result#
                   :form '~f'})
          result#)))))

user=> (->> (range 10)
            #pp->> (filter odd?)
            (map inc))

{:result (1 3 5 7 9), :form (filter odd?)}
=> (2 4 6 8 10)
```

### Configuration-based debugging

Not unique to hash-meta, but it can be handy to leave the debugging
tags in your code, and elide them at compile time based on some 
configuration variable.

```clojure
(ns elision
  (:require [sparkofreason.hash-meta.core :refer [defreader-n]]))

(def ^:dynamic *debug?* true)

(defreader-n tap
  (fn [f f' m]
    (if *debug?*
      `(let [r# ~f]
         (tap> {:form '~f'
                :result r#
                :metadata ~m})
         r#)
      `~f)))

(add-tap println)

user=> (inc #tap (* 2 #tap (+ 3 #tap (* 4 5))))
{:result 20, :form (* 4 5), :metadata {:line 19, :column 43}}
{:result 23, :form (+ 3 (* 4 5)), :metadata {:line 19, :column 33}}
{:result 46, :form (* 2 (+ 3 (* 4 5))), :metadata {:line 19, :column 23}}
=> 47

(alter-var-root #'*debug?* (fn [_] false))

user=> (inc #tap (* 2 #tap (+ 3 #tap (* 4 5))))
=> 47

```

[core.unify]: https://github.com/clojure/core.unify
[hashtag]: https://github.com/sparkofreason/hashtag
[hashp]: https://github.com/weavejester/hashp
[spyscope]: https://github.com/dgrnbrg/spyscope
[postmortem]:https://github.com/athos/postmortem
[debux]: https://github.com/philoskim/debux

## License

Copyright © 2020 Dave Dixon, James Reeves

Released under the MIT license.
