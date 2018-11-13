(set-env! :dependencies [[clj-jgit "0.8.10"]
                         [environ "1.1.0"]])

(ns bump-version 
  (:require [clj-jgit.porcelain :as gitp]
            [clojure.java.io :as io]
            [environ.core :refer [env]]
            [clojure.string :as s]))

(def default-identity "id_rsa")
(def version-regexp #"^version *'\d+\.\d+\.\d+-SNAPSHOT'")

(defn build-file
  "Returns th File for the build.gradle in this path"
  [path]
  (let [build-file (format "%s/build.gradle" path)]
    (io/as-file build-file)))

(defn update-version
  "Updates the given build.gradle to that snapshot version"
  [build-file version]
  (let [path (.getAbsolutePath build-file)
        content (line-seq (io/reader build-file))
        new-version (format "version '%s-SNAPSHOT'" version)
        new-content (map #(s/replace %1 version-regexp new-version) content)]
    (with-open [w (io/writer path)]
      (doseq [line new-content] (.write w line) (.write w "\n")))))

(defn bump-version
  "Bump version in build.gradle on a new branch and push upstream"
  [path version]
  (let [repo (gitp/git-init path)
        build-file (build-file path)
        branch (format "b-%s" version)
        msg (format "Bumped version to %s" version)]
    (gitp/git-checkout repo "develop")
    (gitp/git-pull repo "upstream")
    (gitp/git-branch-create repo branch)
    (gitp/git-checkout repo branch)
    (update-version build-file version)
    (gitp/git-add repo (.getName build-file))
    (gitp/git-commit repo msg)
    (gitp/git-push repo "upstream")))

(defn execute-with-identity
  "Executes the function using identity for Git actions"
  [action identity]
  (let [name (format "%s/.ssh/%s" (env :home) identity)]
    (gitp/with-identity {:name name :exclusive true} (action))))

(defn -main
  "Bump version in build.gradle on a new branch and push upstream"
  [& args]
  (let [path (nth args 0)
        version (nth args 1)
        identity (if (= (count args) 3) (nth args 2) default-identity)
        build-file (build-file path)
        has-build-file (.exists build-file)
        git-action #(bump-version path version)]
    (cond
      (and has-build-file version) (execute-with-identity git-action identity)
      has-build-file (println "You must provide a version to bump to")
      (and path version) (println "You must provide the path to a folder with a build.gradle")
      :else (println "You must provide the path to a folder with a build.gradle and a version to bump to"))))
