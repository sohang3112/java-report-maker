(ns java-report-maker.core
  (:require [clojure.reflect :as cr]
            [me.raynes.fs    :as fs])
  (:import  [clojure.lang DynamicClassLoader Reflector]
            [java.net URLClassLoader URL]
            [java.io File]
            [javax.tools DiagnosticCollector ToolProvider]
            [org.apache.commons.io FilenameUtils]
            [org.apache.commons.lang3 ArrayUtils])
  (:gen-class))

;; TODO: rewrite to use clojure.reflect API wherever possible (it's simpler!)
;; TODO: maybe we can do this using import macro somehow?

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(def string-array-class (.getClass (into-array [""])))

(def java-dir "D:/java-sample-programs")
(def java-dir-file (File. java-dir))
(def java-ls (fs/glob java-dir-file "*.java"))

(def java-file (first java-ls))

; TODO: use DynamicClassLoader (is it simpler? need to check..)

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
(if (.call task)
  ;; Successful Compile
  (let [class-loader (URLClassLoader. [(.. java-dir-file toURI toURL)]),
        dynamic-class (->> java-file
                           .getName
                           FilenameUtils/getBaseName
                           (.loadClass class-loader))]

    ;; NOTE: If the dynamic main method being called prints to the terminal,
    ;; it will show in the terminal only, not in the REPL output.
    (with-out-str (Reflector/invokeStaticMethod dynamic-class "main"
                                  (object-array [(into-array ["arg1" "arg2"])]))))
  ;; Compilation Error
  (doseq [diag (.getDiagnostics diagnostics)]
    (println (str diag))))

;; finally (cleanup)

;(.close file-manager)