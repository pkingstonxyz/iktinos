(require '[clojure.java.io :as io]
         '[clojure.edn :as end])

;(doseq [arg *command-line-args*]
;  (println (str "Read argument:" arg)))

(edn/read (slurp "schematic.edn"))
