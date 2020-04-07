(ns forest.graphics.textures
  (:require [quil.core :as q]))

(def
  ^{:doc "Coverage texture granularity.

Sets the granularity of coverage representation. Assuming that coverage
is expressed by a percentage, this value states how many possible texture
levels there can be. This is basiclly `(/ 100 coverage-granularity)`
levels. So if the value is 10, there will be 10 textures, representing
each decile. If the value is 25, there will only be 4 textures - one
for each quartile."}
  coverage-granularity 25)

(def
  ^{:doc "How many pixels for each square of a texture.

Assuming that the granularity of coverage is n%, this means that
each texture will represent `100 / n` possible coverage squares.
So each texture will be `texture-pixels-per-square * 100 / n` pixels
wide and `texture-pixels-per-square` pixels high."}
  texture-pixels-per-square 100)

(defn tex-offset
  "Return the offset in the texture for the given coverage.

  The actual texture to be used is (step = texture-pixels-per-square):

   ((tex-offset coverage), 0)        ((+ (tex-offset coverage) step), 0)

   ((tex-offset coverage), step)     ((+ (tex-offset coverage) step), step)
  TODO: tests
  "
  [coverage]
  (* (Math/floor (* (/ coverage 100) coverage-granularity))
     texture-pixels-per-square))

(defn coord-scaler
  "Scale `i` from world coords to pixel coords."
  [i item]
  [(* i texture-pixels-per-square) item])

(defn iterate-grid
  "Return an iterator over all the squares in the provided `grid`.

  Each item will consist of `[x y square]`, where `x` and `y` are the
  world coords of the given square, and `square` is the actual square.
  TODO: tests
  "
  [grid]
  (let [x-offset (* 0.5 texture-pixels-per-square (dec (count (first grid))))
        y-offset (* 0.5 texture-pixels-per-square (dec (count grid)))]
    (for [[y row] (map-indexed coord-scaler grid)
          [x item] (map-indexed coord-scaler row)]
      [(- x x-offset) (- y y-offset) item])))

(defn draw-plant-texture
  "Draw the given plant onto the given canvas.

  `grid` should contain the world, i.e. a 2d vector of squares, where
  each square has a `:plants` key containing per plant ground coverage
  in percentages. The provided `texture` should contain textures for
  each coverage step, which will be rendered onto the provided `canvas`.
  "
  [grid canvas texture plant]
  (q/with-graphics canvas
    (q/texture texture)
    (q/no-stroke)
    (doseq [[x y item] (iterate-grid grid)]
      (let [step texture-pixels-per-square
            tex-pos (tex-offset (-> item :plants plant))]
        (q/begin-shape)
        (q/vertex x y 0 tex-pos 0)
        (q/vertex (+ x step) y 0 (+ tex-pos step) 0)
        (q/vertex (+ x step) (+ y step) 0 (+ tex-pos step) step)
        (q/vertex x (+ y step) 0 tex-pos step)
        (q/end-shape)))))


;; Texture generators

(defn random-flowers
  "Generate random flowers with the given parameters.

  This pretends that circles are really rectangles and on the basis of
  that calculates how many flowers to draw to get the desired coverage.
  It's good enough for testing..."
  [n m coverage radius petals center]
  (let [diameter (* 2 radius)
        per-row (Math/ceil (/ n diameter))   ; the number of circles that can be packed in one row
        per-col (Math/ceil (/ m diameter))   ; the number of circles that can be packed in one column
        total-circles (* per-row per-col)    ; the total number of circles that can be packed in the area
        n-circles (Math/ceil (* total-circles (/ coverage 100)))]
    (doseq [i (range n-circles)]
      (let [x (rand-int n) y (rand-int m)]
        (apply q/stroke petals)
        (q/stroke-weight diameter)
        (q/point x y)

        (when (not= petals center)
          (apply q/stroke center)
          (q/stroke-weight 3)
          (q/point x y))))))

(defn make-flowers-texture
  "Return a texture for the given flower definition.

  A flower is simple a coloured circle of radius `radius` with an
  optional second circle in the middle. `petals` sets the colour
  of the flower, `center` sets the colour of the (optional) center.

  This will return a texture with as many squares as are needed to
  represent all the possible granularities, i.e. `(/ 100 coverage-granularity)`.
  Each square consequetive square is the texture for a given
  coverage, going along the x axis.

  If, e.g. coverage-granularity is 4 and texture-pixels-per-square is 100,
  then the resulting texture will be 400x100, where
  * (0, 0)   to (100, 100) - the texture for values <0, 25)
  * (100, 0) to (200, 100) - the texture for values <25, 50)
  * (200, 0) to (300, 100) - the texture for values <50, 75)
  * (300, 0) to (400, 100) - the texture for values <75, 100)
  "
  ([radius petals] (make-flowers-texture radius petals petals))
  ([radius petals center]
   (let [size texture-pixels-per-square
         texture (q/create-graphics (+ (tex-offset 100) size) size)
         step (Math/ceil (/ 100 coverage-granularity))]
     (q/with-graphics texture
       (doseq [i (range coverage-granularity)]
         (random-flowers size size (* step i) radius petals center)
         (q/translate size 0 0)))
     texture)))
