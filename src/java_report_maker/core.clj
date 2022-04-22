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
(def java-compiler (ToolProvider/getSystemJavaCompiler))


(defn reflect-find-method 
  "Finds all method overloads corresponding to `method-symbol` in `object`.
   
   For Example: 

   ```clojure
   (first (reflect-find-method System/out 'println))
   ``` 

   outputs information about the first overload of the Java method
   `System.out.println`:
   
   ```clojure
   {:name println,
    :return-type void,
    :declaring-class java.io.PrintStream,
    :parameter-types [long],
    :exception-types [],
    :flags #{:public}}
   ```"
  [object method-symbol]
  (->> object 
      cr/reflect 
      :members 
      (filter #(= (:name %) method-symbol))))

;; TODO: Refactor - this is ugly (too nested) !!
(with-open [file-manager (.getStandardFileManager java-compiler diagnostics nil nil)]
  (if (.call
       (.getTask java-compiler
                 nil   ;; out
                 file-manager
                 diagnostics
                 ["-classpath" java-dir]   ;; options
                 nil   ;; classes
                 (.getJavaFileObjectsFromFiles file-manager [java-file])))

    (let [class-loader (-> [(.. java-dir-file toURI toURL)]
                           into-array
                           URLClassLoader.),
          dynamic-class (->> java-file
                             .getName
                             FilenameUtils/getBaseName
                             (.loadClass class-loader)),

        ;; NOTE: with-out-str (standard Clojure macro) didn't work here
        ;; So I had to make this custom macro with-stdout-str
          main-output (with-stdout-str
                        (Reflector/invokeStaticMethod
                         dynamic-class
                         "main"
                         (object-array [(into-array ["arg1" "arg2"])])))]
      (println "COMPILED SUCCESSFULLY")
      (println "MAIN OUTPUT:")
      (println main-output))
    (doseq [diag (.getDiagnostics diagnostics)]
      (println "COMPILE ERROR:")
      (println (str diag)))))