# forest

Simulate forest growth

## Setup

Make sure [Leiningen](https://github.com/technomancy/leiningen) is installed.

run `lein dev` to start a development version in your browser, or see `STRUCTURE.md` for
(a lot) more information.

## Building

To make a production version, run `lein prod`, which will compile and minify everything -
the result can be found in `resources/public/`, which can be served as static files.
An example build can be found [here](https://mruwnik.github.io/forest/).

## Data structure

The map is currently based on a height map of Białowieża (`resources/bialawieza.png`). The app reads 'resources/public/bialawieza.edn' and creates an OpenGL model on its basis. The edn file consists of a two dimension array of points, where each point has a height (in metres) and a water content (in percentages), e.g. `{:h 45.1 :w 0.23}`. Water contents larger than 1 mean that the given point is `(- (:w point) 1)` metres under water.
