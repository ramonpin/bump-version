(ns bump-version.core
  (:require [clj-jgit.porcelain :as gitp]
            [clojure.java.io :as io]
            [environ.core :refer [env]]
            [clojure.string :as s])
  (:gen-class))

(def default-identity "id_rsa")
(def version-regexp #"^version = \d+\.\d+\.\d+-SNAPSHOT")

(defn version-file
  "Returns the File that contains version numbiner in this path"
  [path]
  (let [version-file (format "%s/gradle.properties" path)]
    (io/as-file version-file)))

(defn update-version
  "Updates the given version-file to that snapshot version"
  [version-file version]
  (let [path (.getAbsolutePath version-file)
        content (line-seq (io/reader version-file))
        new-version (format "version = %s-SNAPSHOT" version)
        new-content (map #(s/replace %1 version-regexp new-version) content)]
    (with-open [w (io/writer path)]
      (doseq [line new-content] (.write w line) (.write w "\n")))))

(defn bump-version
  "Bump version in build.gradle on a new branch and push upstream"
  [path version]
  (let [repo (gitp/git-init path)
        version-file (version-file path)
        branch (format "b-%s" version)
        msg (format "chore: Bumped version to %s" version)]
    (gitp/git-checkout repo "develop")
    (gitp/git-pull repo "upstream")
    (gitp/git-branch-create repo branch)
    (gitp/git-checkout repo branch)
    (update-version version-file version)
    (gitp/git-add repo (.getName version-file))
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
        version-file (version-file path)
        has-version-file (.exists version-file)
        git-action #(bump-version path version)]
    (cond
      (and has-version-file version) (execute-with-identity git-action identity)
      has-version-file (println "You must provide a version to bump to")
      (and path version) (println "You must provide the path to a folder with a gradle.properties")
      :else (println "You must provide the path to a folder with a gradle.properties and a version to bump to"))))

