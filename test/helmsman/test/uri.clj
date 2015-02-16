(ns helmsman.test.uri
  (:require
    [helmsman.uri]
    [clojure.test :as t]
    [clojure.test.check :as tc]
    [clojure.test.check.generators :as gen]
    [clojure.test.check.properties :as prop]))

(def path-vector-gen
  (gen/vector gen/string-alphanumeric)) 

(def clean-vector-gen
  (gen/vector (gen/such-that not-empty gen/string-alphanumeric)))

(def path-string-gen
  (gen/fmap helmsman.uri/assemble path-vector-gen))

;;; Ensures that noramlization strips out empty path items.
(def uri-path-flat-normalize-property
  (prop/for-all
    [item path-vector-gen]
    (let [normalized (helmsman.uri/normalize-path item)
          stripped (vec (filter not-empty item))]
      (= normalized stripped)))) 
 
;;; Ensures that assembly->parse->assembly is reversible.
(def uri-assemble-parse-property
  (prop/for-all
    [item clean-vector-gen]
    (let [p (helmsman.uri/assemble item)
          s (helmsman.uri/normalize-path
              (helmsman.uri/path p))]
      (= item s))))

(t/deftest uri-testing
  (t/is (:result (tc/quick-check 100 uri-path-flat-normalize-property)))
  (t/is (:result (tc/quick-check 100 uri-assemble-parse-property))))

