(ns forest.graphics.quil
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [re-frame.core :as re-frame]
            [forest.subs :as subs]))

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

(defn points-2d-vertices [n m]
  (for [y (range m)
        x (range n)]
    [[x y] [x (inc y)] [(inc x) y]
     [(inc x) y]
     [(inc x) (inc y)]
     [x (inc y)]]))

(defn draw-triangles
  "Renders the whole grid using the given colour function.

  The idea is for various layers (e.g. water, daisies) to be able to use different renderers."
  [{:keys [grid texture-points texture pixels-per-square]} colour-func]
  (q/with-graphics texture
    (q/begin-shape :triangles)
    (doseq [[x y] texture-points]
      (colour-func grid x y)
      (q/vertex (* x pixels-per-square) (* y pixels-per-square) 0))
    (q/end-shape)))

(defn draw-ground [state]
  (draw-triangles state
                  (fn [grid x y]
                    (q/stroke (* x 2) (* 2 y) 150)
                    (q/fill (* x 2) (* 2 y) 150)))
  state)

(defn setup []
  "Set initial state for graphics."
  (let [grid @(re-frame/subscribe [::subs/world])
        n (dec (count (first grid)))
        m (dec (count grid))]
    {:pixels-per-square 20
     :texture (q/create-graphics (* 20 n) (* 20 m))  ; this should contain the rendered forest
     :texture-points (->> (points-2d-vertices n m)   ; all coords used by the texture
                          (apply concat)
                          (into []))}))


(defn update-world
  "Update the graphics state.

  If the world state has changed, render it to the texture, otherwise just use whatever is present."
  [{prev-grid :grid :as state}]
  (let [grid @(re-frame/subscribe [::subs/world])]
    (if (= prev-grid grid)
      state
      (-> state (assoc :grid grid) draw-ground))))


(defn draw [{world :world ground :texture :as state}]
  (q/background 255)
  (q/lights)
  (q/fill 150 100 150)

  (when ground
    (q/begin-shape)
    (q/texture ground)
    (q/plane 600 600)
    (q/end-shape :close)
    )
  )

(defn start-drawing [canvas-id]
  (q/sketch
   :host canvas-id
   :setup setup
   :draw draw
   :update update-world
   :size [1200 800]
   :renderer :p3d
   :middleware [m/fun-mode m/navigation-3d]))
