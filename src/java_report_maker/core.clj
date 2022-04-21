(ns java-report-maker.core
  (:require [clojure.reflect :as cr]
            [me.raynes.fs    :as fs])
  (:import  [clojure.lang DynamicClassLoader Reflector]
            [java.net URLClassLoader URL]
            [java.io File ByteArrayOutputStream PrintStream FileOutputStream
             FileDescriptor]
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

;; Below macros with-stdout, with-stdout-str adapted from:
;; https://stackoverflow.com/a/4183433/12947681

(defmacro with-stdout [stdout & body]
  "Redirects stdout, executes body, and then resets stdout,
   NOTE: only works for Java methods, doesn't work with Clojure code"
  `(try
     (System/setOut ~stdout)
     ~@body
     (finally
       (-> FileDescriptor/out FileOutputStream. PrintStream. System/setOut))))

(defmacro with-stdout-str [& body]
  "Captures stdout of Java method as a string.
   NOTE: doesn't work for Clojure code"
  `(let [baos# (ByteArrayOutputStream.)]
     (with-stdout (PrintStream. baos#) ~@body)
     (.toString baos#)))

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
  (let [class-loader (-> [(.. java-dir-file toURI toURL)]
                         into-array
                         URLClassLoader.),
        dynamic-class (->> java-file
                           .getName
                           FilenameUtils/getBaseName
                           (.loadClass class-loader))
        main-output (with-stdout-str
                      (Reflector/invokeStaticMethod
                       dynamic-class
                       "main"
                       (object-array [(into-array ["arg1" "arg2"])])))]
    (println "MAIN OUTPUT:")
    (println main-output)
  ;; Compilation Error ;; NOTE: with-out-str (standard Clojure macro) didn't work here
    (doseq [diag (.getDiagnostics diagnostics)]
      (println "COMPILE ERROR:")
      (println (str diag)))))

;; finally (cleanup)

;(.close file-manager)