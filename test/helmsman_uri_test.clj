(ns helmsman-uri-test
  (:require
    [clojure.test :refer :all]
    [helmsman.uri :as uri]))

(def static-uri-path-1 ["working" "on" "the" "railroad"])
(def static-uri-path-2 ["working" "with" "people"])
(def static-uri-path-3 ["user" "profile" :user-id])

(def static-user-id 12345)

(def static-uri-1 "working/on/the/railroad")
(def static-uri-2 "working/with/people")
(def static-uri-3 (str "user/profile/" static-user-id))

(def static-relative-1 "../../with/people")
(def static-relative-2 "../on/the/railroad")
(def static-relative-3 (str "../../../user/profile/" static-user-id))

(def dynamic-path-1 "user/:operation/:user-id")
(def dynamic-path-1-final "user/update/12345")
(def dynamic-path-2 "forum/post/:user-id/:thread-id")
(def dynamic-path-2-final "forum/post/12345/54321")

(deftest basic-uris
  (testing "Testing basic uri creation."
    (is (= static-uri-1 (uri/assemble static-uri-path-1)))
    (is (= static-uri-2 (uri/assemble static-uri-path-2))))
  (testing "Testing basic uri creation with a variable uri segment"
    (is (= static-uri-3 (uri/assemble static-uri-path-3 :user-id static-user-id))))
  (testing "Testing relative URI creation"
    (is (= static-relative-1 (uri/assemble
                               (uri/relative-uri
                                 static-uri-path-1
                                 static-uri-path-2))))
    (is (= static-relative-2 (uri/assemble
                               (uri/relative-uri
                                 static-uri-path-2
                                 static-uri-path-1)))))
  (testing "Testing relative URI creation with a variable uri segment"
    (is (= static-relative-3 (uri/assemble
                               (uri/relative-uri
                                 static-uri-path-1
                                 static-uri-path-3)
                               :user-id static-user-id)))))

(deftest dynamic-uris
  (testing "Re-writing URIs"
    (is (= dynamic-path-1-final (uri/assemble (uri/path dynamic-path-1)
                                              :operation "update"
                                              :user-id "12345")))
    (is (= dynamic-path-2-final (uri/assemble (uri/path dynamic-path-2)
                                              :thread-id "54321"
                                              :user-id "12345")))
    (is (= dynamic-path-1 (uri/assemble (uri/path dynamic-path-1))))
    (is (= dynamic-path-2 (uri/assemble (uri/path dynamic-path-2))))))
