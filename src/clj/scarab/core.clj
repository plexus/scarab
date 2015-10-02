(ns scarab.core
  (:require [endophile.core :as md]
            [compojure.core :as http]
            [compojure.route :as route]
            [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as gitq]))

(def repo-dir "/home/arne/projects/scarab-repos/")

(defn git-repo-by-name [name]
  (git/load-repo (str repo-dir name)))

(defn git-commit-by-id [repo commit-id]
  (gitq/find-rev-commit repo
                        (clj-jgit.internal/new-rev-walk repo)
                        commit-id))

(defn git-list-paths
  "Returns a lazy seq of all file pathnames in the tree of a given commit. Does
  not list directories"
  [repo commit]
  (let [treewalk (org.eclipse.jgit.treewalk.TreeWalk. (.getRepository repo))]
    (doto treewalk
      (.addTree (.getTree commit))
      (.setRecursive true))
    (letfn [(nextfn []
              (if (.next treewalk)
                (cons (.getPathString treewalk) (lazy-seq (nextfn)))
                nil))]
      (nextfn))))

(def source-repo (git-repo-by-name "ClojureBridge-organizing"))
(def target-repo (git-repo-by-name "ClojureBridge-organizing-nl"))



;; (println
;;  (let [repo source-repo
;;        patch (gitq/changed-files-with-patch repo (first (git/git-log repo)))
;;        info (gitq/commit-info repo (first (git/git-log repo)))]
;;    info))

;; md

;; (let [tags (-> "/home/arne/github/ClojureBridge-organizing/Workshop-Planning-Tasks.md"
;;                slurp
;;                md/mp
;;                md/to-clj)]
;;   (doseq [x tags]
;;     (println (:tag x))))

(defn action-list-files [repo-name commit-id]
  (let [repo (git-repo-by-name repo-name)
        commit (if (= commit-id "~")
                 (-> repo git/git-log first)
                 (git-commit-by-id repo commit-id))]
    (git-list-paths repo commit)))


;; (http/defroutes app-routes
;;   (http/GET ["/repo/:repo/commit/:commit/ls"] [repo commit] (render user-view {:id id}))
;;   (route/not-found "Not Found"))




;; org.eclipse.jgit.treewalk.TreeWalk treeWalk = new TreeWalk(repository);
;; treeWalk.addTree(tree);
;; treeWalk.setRecursive(false);
;; while (treeWalk.next()) {
;;     if (treeWalk.isSubtree()) {
;;         System.out.println("dir: " + treeWalk.getPathString());
;;         treeWalk.enterSubtree();
;;     } else {
;;         System.out.println("file: " + treeWalk.getPathString());
;;     }
;; }


;; 8e27230a46d3c88d
