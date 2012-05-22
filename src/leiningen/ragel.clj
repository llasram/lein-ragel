(ns leiningen.ragel
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [leiningen.core.main :as main])
  (:import [java.io File]))

(defn- stale-ragel-targets
  "Returns a lazy seq of [source, compiled] tuples for every Ragel source file
within `dirs` modified since it was most recently compiled."
  [dirs compile-path]
  (for [dir dirs
        ^File source (filter #(-> ^File % (.getName) (.endsWith ".rl"))
                             (file-seq (io/file dir)))
        :let [rel-source (.substring (.getPath source) (inc (count dir)))
              rel-compiled (.substring rel-source 0 (- (count rel-source) 3))
              compiled (io/file compile-path rel-compiled)]
        :when (>= (.lastModified source) (.lastModified compiled))]
    [source compiled]))

(defn- ragel-command
  "Compile all sources of possible options and add important defaults."
  [project args source compiled]
  (concat [(:ragel-command project) "-J"] (:ragel-options project) args
          ["-o" (.getPath compiled) (.getPath source)]))

(defn run-ragel-target
  [project args [source compiled]]
  (.mkdirs (.getParentFile compiled))
  (let [command (ragel-command project args source compiled)
        {:keys [exit out err]} (apply sh/sh command)]
    (when-not (.isEmpty out) (print out))
    (when-not (.isEmpty err) (print err))
    (when-not (zero? exit)
      (main/abort "lein-ragel: `ragel` execution failed"
                  "with status code" exit))))

(defn- run-ragel-task
  "Run ragel to compile all source file in the project."
  [project args]
  (let [compile-path (:ragel-compile-path project)
        source-paths (:ragel-source-paths project)
        targets (stale-ragel-targets source-paths compile-path)]
    (when (seq targets)
      (main/info "Compiling" (count targets) "Ragel source files to" compile-path)
      (dorun (map (partial run-ragel-target project args) targets)))))

(defn ragel-defaults
  [project]
  (let [compile-path (.getPath (io/file (:target-path project) "ragel"))]
    {:ragel-compile-path compile-path
     :ragel-options []
     :ragel-command "ragel"}))

(defn ragel
  "Compile Ragel source files.

Add a :ragel-source-paths key to project.clj to specify where to find them.
Will compile the files to :ragel-compile-path, which defaults to 'target/ragel'.
You will probably want to add :ragel-compile-path to your :java-source-paths.
Provide :ragel-command to specify the command used to run Ragel (default
'ragel') and :ragel-options for any additional arguments to pass to each Ragel
command instance.

Any options passed to the task will also be passed to each Ragel command
instance."
  [project & args]
  (let [project (merge (ragel-defaults project) project)]
    (run-ragel-task project args)))