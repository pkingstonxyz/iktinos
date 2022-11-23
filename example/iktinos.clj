(require '[clojure.java.io :as io]
         '[clojure.edn     :as edn]
         '[babashka.fs     :as fs])

;; Read in the schematic file
(def schematic
  (edn/read-string (slurp "schematic.edn")))

;; Create the base of every static site
(doseq [file ["index.html" "index.css" "styles.css"]] (fs/create-file file))

;; Make pages
(defn append-link [nm]
  "Appends a link to the html file in index.html"
  (spit "index.html" (str "<!--./" nm "/" nm ".html-->\n") :append true))

(defn populate-with-base [nm]
  "Populates a /nm/nm.html file with the proper html"
  (if (fs/exists? (str "./templates/" nm ".html"))
    (fs/copy (str "./templates/" nm ".html") (str "./" nm "/" nm ".html"))
    (fs/copy "./templates/base.html" (str "./" nm "/" nm ".html"))))

(defn render-base [nm attrs]
  "Renders the base file into the right file"
  (let [filename (str "./" nm "/" nm ".html")]
    (spit 
      filename
      (reduce #(clojure.string/replace %1 (str (key %2)) (val %2)) (slurp filename) attrs))))

(defn create-content-page [{nm    :name
                            attrs :attrs}]
  "Creates a content page"
  (fs/create-dir nm) ;Create folder
  (populate-with-base nm) ;Puts the base file contents in the html
  (render-base nm attrs) ;Renders the base file to a page
  (append-link nm)) ;Put links in index.html

(defn create-index-page [{nm    :name
                          attrs :attrs}]
  "Creates an index page"
  (doseq [dir (map #(str "./" nm "/" %) ["content" "pages"])] (fs/create-dirs dir))
  (populate-with-base nm) ;Puts the base file contents in the html
  (render-base nm attrs) ;Renders the base file to a page
  (append-link nm)) ;Put links in index.html

(defn create-page [page]
  "Creates a cpage or an ipage given page"
  (cond 
    (= (page :type) :content) (create-content-page page)
    (= (page :type) :index) (create-index-page page)
    :else (throw (Exception. (str page " does not have a valid type")))))

(doseq [page (schematic :pages)] (create-page page))
