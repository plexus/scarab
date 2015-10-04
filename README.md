# Scarab

App for managing translation of a repo of markdown files

## Overview

## Setup

Start figwheel, which basically handles everything.

    lein figwheel

This takes care of

* launching a ring web server, main entry point at [http://localhost:3449/index.html](http://localhost:3449/index.html)
* auto-reloading CLJS and CSS
* starting a browser-connected REPL
* providing an nREPL

The figwheel process will give you a Clojurescript REPL as soon as you've loaded
the page in the browser. You can also connect to this process at port 7888, e.g.
with CIDER, to get a Clojure REPL.

To drop into a browser REPL from Clojure, do

```
user> (use 'figwheel-sidecar.repl-api)
user> (cljs-repl)
```

To edit the styles (CSS) use Garden. Style definitions are done in Clojure. Changes will trigger a compile from CLJ to CSS. Figwheel will make sure the browser instantly updates.

    lein garden auto

To clean all compiled files:

    lein clean

To create a production build run:

    lein cljsbuild once min

## License

Copyright © 2015 Arne Brasseur

Distributed under the [Mozilla Public License 2.0](https://www.mozilla.org/en-US/MPL/2.0/)
