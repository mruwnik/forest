(ns forest.graphics.quil
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [re-frame.core :as re-frame]
            [forest.subs :as subs]
            [forest.graphics.textures :as tex :refer [coverage-granularity texture-pixels-per-square]]
            ))

;; FIXME: The current way is pretty much backwards - sending all the vertices
;; takes ages, so there are now 3 mechanisms - the render loop (handled by quil),
;; frontend actions (handled by re-frame) and the model updates handles pretty much
;; ad hoc (or via re-frame events). Assuming that the models get updated e.g. every
;; 3 seconds, and that the render also takes a while, it seems that to make things
;; resposive, quil should only send stuff to shaders every now and then, while in the
;; mean time making use of buffered data. I can't be bothered to set up shaders now,
;; so it currently works by rendering to a texture on model updates, and then the
;; texture is used on a basic rectangle. Until proper 3d objects are available this
;; should suffice (although it's really ugly and generally a bad idea) - trees and
;; bushes can be rendered seperately from the ground cover, as there will be a lot
;; less of 'em and a simple stick and ball should do for now

(defn draw-world
  "Renders the whole grid using layer specific colour functions.

  The idea is for various layers (e.g. water, daisies) to be able to use different renderers."
  [{:keys [grid grid-canvas pixels-per-square textures] :as state}]
  (q/with-graphics grid-canvas (q/background 116 102 59))

  (doseq [plant [:grass :daisies]]
    (tex/draw-plant-texture grid grid-canvas (textures plant) plant)))

(defn setup
  "Set initial state for graphics.

  This will also load all plant specific textures - which can take a while."
  []
  (let [grid @(re-frame/subscribe [::subs/world])
        n (* texture-pixels-per-square (dec (count (first grid))))
        m (* texture-pixels-per-square (dec (count grid)))]
    {:grid-canvas (q/create-graphics n m :p3d)  ; this should contain the rendered forest
     :textures {:water nil
                :daisies (tex/make-flowers-texture 5 [255 255 255] [255 155 0])
                :grass (tex/make-flowers-texture 1 [0 255 0])
                }}))

(defn update-world
  "Update the graphics state.

  If the world state has changed, render it to the texture, otherwise just use whatever is present."
  [{prev-grid :grid :as state}]
  (let [grid @(re-frame/subscribe [::subs/world])]
    (if (= prev-grid grid)
      state
      (let [state (assoc state :grid grid)]
        (draw-world state)
        state))))

(defn draw
  "Draw the world.

  Coz it's slow, the current state is rendered to a texture elsewhere - this simply
  displays it on a rectangle. This allows moving around to work relatively fluidly."
  [{world :world ground :grid-canvas :as state}]
  (q/background 255)
  (q/lights)
  (q/fill 150 100 150)

  (when ground
    (q/begin-shape)
    (q/texture ground)
    (q/rect 0 0 600 600)
    (q/end-shape :close)))

(defn start-drawing [canvas-id]
  (q/sketch
   :host canvas-id
   :setup setup
   :draw draw
   :update update-world
   :size [1200 800]
   :renderer :p3d
   :middleware [m/fun-mode m/navigation-3d]))
