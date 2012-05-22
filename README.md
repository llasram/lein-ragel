# lein-ragel

A Leiningen plugin to compile Ragel state machines to Java source files.

## Usage

Put `[lein-ragel "0.1.0"]` into the `:plugins` vector of your `project.clj` and
set `:ragel-source-paths` to the path to your Ragel source files.

Then compile any stale Ragel source files to Java source files by running:

    $ lein ragel

For optimal usefulness, you will probably want to include `:ragel-compile-path`
(default `target/ragel`) in your `:java-source-paths` and `ragel` in your
`:prep-tasks`.  Example:

```clj
(defproject example-project "0.1.0-SNAPSHOT"
  ...
  :plugins [[lein-ragel "0.1.0"]]
  :java-source-paths ["target/ragel"]
  :ragel-source-paths ["src/ragel"]
  :prep-tasks [ragel javac]
  ...)
```

You may specify the Ragel command to use with `:ragel-command` and any other
options to pass to Ragel with `:ragel-options`.

## License

Copyright © 2012 Marshall T. Vandegrift

Distributed under the Eclipse Public License, the same as Clojure.
