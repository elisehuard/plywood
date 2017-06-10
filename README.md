# plywood

A clojure library intended to bring functionality of [dplyr](https://cran.r-project.org/web/packages/dplyr/dplyr.pdf) to clojure.core.matrix.dataset.

## Usage

### Filter function
Takes a vector of columns, and a corresponding filter
predicate function taking the values of those columns and returning a
boolean-like.

```clojure
 (filter-dataset test-ds1 ["a" "b" "c"] (fn [a b c] (and (> a b) (not (nil? c)))))
```

### Join
_left-join_: takes all the rows on the left dataset and attempts to
match them by the columns on the right dataset. If no match is found
the row is filled with nils.  If more than one match is found, more
than one row is created, and the dataset is returned.
```clojure
(left-join test-ds1 test-ds2 [:a])
```

_right-join_: symmetrical to left-join.

``` clojure
(right-join test-ds1 test-ds2 [:a]
```

_inner-join_: if no match is found in the other dataset, the row is
simply dropped.


## Roadmap

TODO: join on different column names using maps for equality
group-by - summarize using functions
order rows defining ordering function

## Credits

The filter and join functions were started by my esteemed colleagues Tom Coupland and Antony
Woods.

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
