(ns azql-mssql.core
  (:require azql-mssql.dialect))

; stolen from 'korma'
(defn mssql-db
  "Create a database specification for a mssql database. 
   Opts should include keys for :db, :user, and :password. 
   You can also optionally set host and port."
  [{:keys [user password db host port] :as opts}]
  (let [host (or (:host opts) "localhost")
        port (or (:port opts) 1433)
        user (or user "dbuser")
        password (or password "dbpassword")
        db (or (:db opts) "")]
    (merge 
      {:classname "net.sourceforge.jtds.jdbc.Driver"
       :subprotocol "jtds:sqlserver"
       :user        user
       :password    password
       :subname (str "//" host ":" port "/" db)}
      opts)))

