(ns azql-mssql.core
  (:use [azql dialect render emit])
  (:require [clojure.string :as s]))

(register-dialect ::mssql)

(def BETWEEN (raw "BETWEEN"))
(def TOP (raw "TOP"))

(defmethod guess-dialect :jtds
  [_]
  ::mssql)

(defmethod naming-strategy ::mssql
  []
  {:entity (fn [x] (str \[ x \]))
   :keyword s/lower-case})

(defmethod render-limit ::mssql
  [_]
  NONE)

(defn render-select-body
  [relation render-orderby?]
  [SELECT
   (render-modifier relation)
   (render-fields relation)
   (render-from relation)
   (render-where relation)
   (if render-orderby? (render-order relation) NONE)
   (render-group relation)
   (renger-having relation)])

(defmethod render-select ::mssql
  [{:keys [offset limit] :as relation}]
  (if (or offset limit)
    (let [start (or offset 1)
          end (+ -1 start (or limit (max-limit-value)))]
      [(raw "SELECT * FROM (SELECT *, row_number() OVER (")
       (if (:order relation)
         (render-order relation)
         (raw "ORDER BY CURRENT_TIMESTAMP"))
       (raw ") AS _azql_rownum FROM (")
       (render-select-body relation false)
       (raw ") AS _azql_query ) AS _azql_numberedqeery")
       WHERE :_azql_numberedqeery._azql_rownum BETWEEN start AND end])
    (render-select-body relation true)))
