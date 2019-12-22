# hash-f

hash-f is derived from a fork of [weavejester/hashp][], and allows
definition of "tagged literal" functions to aid in debugging. If
"hashp (ab)uses data readers to make it easier to get debugging 
data", hash-f beats them with a sh\*\*ty stick to achieve the same,
but with more flexibility.

[weavejester/hashp]: https://github.com/weavejester/hashp

## Usage

To define 

```clojure
(ns example.core)

(defn mean [xs]
  (/ (double #p (reduce + xs)) #p (count xs)))
```

It's faster to type than `(prn ...)`, returns the original result, and
produces more useful output by printing the original form, function
and line number:

```
user=> (mean [1 4 5 2])
#p[example.core/mean:4] (reduce + xs) => 12
#p[example.core/mean:4] (count xs) => 4
3.0
```

## Install

### Leiningen

Add the following to `~/.lein/profiles.clj`:

```edn
{:user
 {:dependencies [[hashp "0.1.0"]]
  :injections [(require 'hashp.core)]}}
```

### Boot

Add the following to `~/.boot/profile.boot`:

```clojure
(set-env! :dependencies #(conj % '[hashp "0.1.0"]))

(require 'hashp.core)
(boot.core/load-data-readers!)
```

## License

Copyright © 2019 James Reeves

Released under the MIT license.
