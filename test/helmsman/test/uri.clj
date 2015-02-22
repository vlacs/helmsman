(ns helmsman.test.uri
  (:require
    [helmsman.uri]
    [ring.util.codec]
    [clojure.test :as t]
    [clojure.test.check :as tc]
    [clojure.test.check.generators :as gen]
    [clojure.test.check.properties :as prop]))

(def path-element-gen
  (gen/one-of
    [gen/string-alphanumeric
     gen/keyword]))

(def path-vector-gen
  (gen/vector
    path-element-gen))

(def path-string-gen
  (gen/fmap helmsman.uri/assemble path-vector-gen))

(def normalization-size-property
  (prop/for-all
    [item path-vector-gen]
    (let [normalized (helmsman.uri/normalize-path item)]
      (>= (count (flatten item)) (count normalized)))))

(def normalization-reverse-set-property
  (prop/for-all
    [item path-vector-gen]
    (let [normalized (helmsman.uri/normalize-path item)
          normalized-set (set normalized)
          contained
          (vec
            (keep
              (fn [i]
                (when (contains? normalized-set i)
                  i))
              (flatten item)))]
      (= normalized contained))))

(def normalization-subset-property
  (prop/for-all
    [item path-vector-gen]
    (let [normalized (helmsman.uri/normalize-path item)
          normalized-set (set normalized)
          raw-set (set (flatten item))]
      (clojure.set/subset? normalized-set raw-set))))

(def normalized-assembly-commutitive-property
  (prop/for-all
    [item path-vector-gen]
    (let [normalized (helmsman.uri/normalize-path item)
          assembled (helmsman.uri/assemble normalized)
          disassembled (helmsman.uri/path assembled)]
      (= normalized disassembled))))

(t/deftest uri-testing
  (t/testing "Normalization"
    (t/is (:result (tc/quick-check 100 normalization-size-property))
          "Flatted input vector should be the same size or bigger than the output.")
    (t/is (:result (tc/quick-check 100 normalization-reverse-set-property))
          "The input vector containing only items belonging to the set derived
          from the normalized input vector should match the normal vector.")
    (t/is (:result (tc/quick-check 100 normalization-subset-property))
          "The output vector as a set should be a subset of the input.")
    (t/is
      (:result
        (tc/quick-check 100 normalized-assembly-commutitive-property)))))

