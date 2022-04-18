# Notes
- See [here](http://www.unexpected-vortices.com/clojure/some-practical-examples/files-and-directories.html) for usage examples of `me.raynes.fs` - a library that allows us to use glob patterns (among other things).
- Dynamically compiling and loading Java classes:
    - [OpenHFT Java Runtime Compiler (external)](https://stackoverflow.com/a/26117269/12947681) - *simpler approach*
        - **PROBLEM:** Can't manage to actually import it!!
        - But `lein deps` says that it has installed it - no idea what's wrong...
    - [javax.tools.JavaCompiler (builtin)](https://stackoverflow.com/a/21544850/12947681)
    - [More examples (misc libraries)](https://www.tabnine.com/code/java/classes/net.openhft.compiler.CachedCompiler)