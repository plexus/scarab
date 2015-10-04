(ns scarab.core
  (:require [endophile.core :as md]
            [compojure.core :as http]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as gitq]
            [org.httpkit.server :as httpkit]
            [clojure.data.json :as json]
            [ring.middleware.reload :refer [wrap-reload]])
  (:import [java.io ByteArrayOutputStream]
           [org.eclipse.jgit.treewalk TreeWalk]))


(def repo-dir "/home/arne/projects/scarab-repos/")

(defn git-repo-by-name [name]
  (git/load-repo (str repo-dir name)))

(defn git-commit-by-id [repo commit-id]
  (gitq/find-rev-commit repo
                        (clj-jgit.internal/new-rev-walk repo)
                        commit-id))


        ;; ObjectId objectId = treeWalk.getObjectId(0);
        ;; ObjectLoader loader = repository.open(objectId);

        ;; // and then one can the loader to read the file
        ;; loader.copyTo(System.out);

        ;; revWalk.dispose();

        ;; repository.close();

(defn git-blob-contents [repo blob-id]
  (let [stream (ByteArrayOutputStream.)]
    (.copyTo (.open (.getRepository repo) blob-id) stream)
    (.toString stream)))

(defn git-list-paths
  "Returns a lazy seq of all file pathnames in the tree of a given commit. Does
  not list directories"
  [repo commit]
  (let [treewalk (TreeWalk. (.getRepository repo))]
    (doto treewalk
      (.addTree (.getTree commit))
      (.setRecursive true))
    (letfn [(nextfn []
              (if (.next treewalk)
                (cons (.getPathString treewalk) (lazy-seq (nextfn)))
                nil))]
      (nextfn))))

(defn git-repo-commit [repo-name commit-id]
  (let [repo (git-repo-by-name repo-name)
        commit (if (= commit-id "~")
                 (-> repo git/git-log first)
                 (git-commit-by-id repo commit-id))]
    [repo commit]))

(defn action-list-files [repo-name commit-id]
  (let [[repo commit] (git-repo-commit repo-name commit-id)]
    (git-list-paths repo commit)))

(defn action-parse-markdown [repo-name commit-id path]
  (println path)
  (let [[repo commit] (git-repo-commit repo-name commit-id)]
    (->> (git/get-blob-id repo commit path)
         (git-blob-contents repo)
         md/mp
         md/to-clj)))

(http/defroutes app-routes
  (http/GET ["/repo/:repo/commit/:commit/ls"] [repo commit]
            (json/write-str (action-list-files repo commit)))
  (http/GET ["/repo/:repo/commit/:commit/files/:filename"] [repo commit filename]
            (json/write-str (action-parse-markdown repo commit filename)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def ring-handler (-> #'app-routes
                      handler/api
                      wrap-reload))


(comment
  ;; done by figwheel now
  (defn start-server []
    (httpkit/run-server ring-handler {:port 8999}))

  (defonce ^:dynamic *server* (start-server))


  (def repo (git-repo-by-name "ClojureBridge-organizing"))

  (println
   (let [repo source-repo
         patch (gitq/changed-files-with-patch repo (first (git/git-log repo)))
         info (gitq/commit-info repo (first (git/git-log repo)))]
     info))

  (let [tags (-> "/home/arne/github/ClojureBridge-organizing/Workshop-Planning-Tasks.md"
                 slurp
                 md/mp
                 md/to-clj)]
    (doseq [x tags]
      (println (:tag x))))
  )
