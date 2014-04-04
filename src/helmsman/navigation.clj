(ns helmsman.navigation
  (:require
    [helmsman.tree :as tree]))

(def navigation-meta-tags
  [:id :name :weight])

(defn pred-by-id
  [id]
  (fn [i]
    (if
      (contains? i :id)
      (let [m-id (get i :id)]
        (if (= m-id id)
          true
          false))
      false)))

(defn meta-from-request
  [request pred-fn]
  (let [meta-data (get-in request [:helmsman :all-meta])]
    (loop [md (vec meta-data)]
      (if (empty? md)
        nil
        (let [i (first md)]
          (if
            (pred-fn i)
            i
            (recur (vec (rest md)))))))))

