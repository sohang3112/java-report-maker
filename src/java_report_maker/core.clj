(ns java-report-maker.core
  (:require [clojure.reflect :refer [reflect Reflector]]
            [me.raynes.fs    :as fs])
  (:import  [java.net URLClassLoader URL]
            [java.io File]
            [javax.tools DiagnosticCollector ToolProvider]
            [org.apache.commons.io FilenameUtils]
            [org.apache.commons.lang3 ArrayUtils])
  (:gen-class))

;; TODO: rewrite to use clojure.reflect API wherever possible (it's simpler!)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(def string-array-class (.getClass (into-array [""])))

(def java-dir "D:/java-sample-programs")
(def java-dir-file (File. java-dir))
(def java-ls (fs/glob java-dir-file "*.java"))

(def java-file (first java-ls))

; IN PROGRESS: Convert Java code in .experimental.java (using JavaCompiler)
; to Clojure

(def diagnostics (DiagnosticCollector.))
(def compiler (ToolProvider/getSystemJavaCompiler))
(def file-manager (.getStandardFileManager compiler diagnostics nil nil))
(def options ["-classpath" java-dir])
(def compile-unit (.getJavaFileObjectsFromFiles file-manager [java-file]))
(def task
  (.getTask compiler
            nil   ;; out
            file-manager
            diagnostics
            options
            nil   ;; classes
            compile-unit))
(.call task)

;; if call task => true

(def class-loader
  (-> java-dir-file
      .toURI
      .toURL
      vector
      into-array
      URLClassLoader.))

(def dynamic-class
  (->> java-file
       .getName
       FilenameUtils/getBaseName
       (.loadClass class-loader)))

(Reflector/invokeStaticMethod dynamic-class "main" 
                              (object-array [(into-array ["arg1" "arg2"])]))

;; if call task => false

(.getDiagnostics diagnostics)