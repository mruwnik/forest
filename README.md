# forest

Simulate forest growth

## Setup

Make sure [Leiningen](https://github.com/technomancy/leiningen) is installed.

The graphics are done with OpenGl, using penumbra as a wrapping. Unfortunatly, there is no new
version on ClojureJars, so it has to be installed manually. To do this following these steps:

    git clone https://github.com/ztellman/penumbra
    cd penumbra
    lein install

## Usage

Call `lein run <height map file>`, e.g. `lein run resources/bialowieza.png`
