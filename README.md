# Scarab

App for managing translation of a repo of markdown files

## Overview

## Setup

Start figwheel, so CLJS and CSS are automatically reloaded

    lein figwheel

This will drop into a browser-connected REPL after opening the browser (see
below), so you have a REPL for evaluating Clojurescript.

Start the ring server, to serve up `index.html`, and also handle API requests

    lein repl
    user> (require 'scarab.core)
    user> (scarab.core/start-server)

Keep this REPL open, you can use it to evaluate Clojure. You can also do this in
your editor, e.g. `cider-jack-in`.

Watch for Garden style definition changes so they recompile from clj to css.
Figwheel will make sure the browser instantly updates.

    lein garden auto

Visit [localhost:8999](http://localhost:8999). This is the ring server. You have
to use this one, and not the server provided by figwheel (`:3499`), or you won't
be able to make XHR requests due to browser XSS same-origin restrictions.

To get an interactive development environment run:

    lein figwheel

To clean all compiled files:

    lein clean

To create a production build run:

    lein cljsbuild once min

## License

Copyright © 2015 Arne Brasseur

Distributed under the [Mozilla Public License 2.0](https://www.mozilla.org/en-US/MPL/2.0/)
