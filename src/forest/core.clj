(ns forest.core
  (:require [forest.geography :as geo]
            [penumbra.app :as app]
            [penumbra.data :as data])
  (:use [penumbra opengl compute])
  (:gen-class))


(defmacro height-vertex [height-map x y]
  "Get a vertex representing a single height point of the terrain."
  `(let [x# ~x y# ~y]
      (vertex x# (nth (nth ~height-map x#) y#) y#)))

(defn terrain-row [height-map line]
  (draw-triangle-strip
   (doseq [x (range (count height-map))]
     (height-vertex height-map x line)
     (height-vertex height-map x (inc line)))))

(defn draw-terrain [height-map]
  (color 101/255 62/255 29/255)
  (run! #(terrain-row height-map %) (range (dec (count height-map)))))

(def light-ambient [0.5 0.5 0.5 1])
(def light-diffuse [1 1 1 1])
(def light-position [0 400 0 1])

(defn init [state]
  (app/title! "Nehe Tutorial 2")
  (app/vsync! false)
  (assoc state :fullscreen false)

  (shade-model :smooth)
  (clear-color 0 0 0 0.5)
  (hint :perspective-correction-hint :nicest)
  (depth-test :lequal)
  (enable :depth-test)
  (light 1
         :ambient light-ambient
         :diffuse light-diffuse
         :position light-position)
  (enable :light1)

  (let [height-map (geo/load-png-height-map "resources/bialowieza.png")]
    (assoc state
           :scene (create-display-list
                  (push-matrix
                   (draw-terrain height-map))
                   (push-matrix
                    (scale 5 1 5)
                    (color 0.5 0.5 0.5)
                    (draw-quads
                     (vertex 10 -1 -11) (vertex -10 -1 -11)
                     (vertex -10 -1 11) (vertex 10 -1 11)))))))

(defn reshape [[x y w h] state]
   (frustum-view 45 (/ w h) 0 300)
  state)

(defn mouse-drag [[dx dy] _ button state]
  (assoc state
    :rot-x (+ (:rot-x state) dy)
    :rot-y (+ (:rot-y state) dx)))

(defn key-press [key state]
  (condp = key
    "w" (update-in state [:z-axis] inc)
    "s" (update-in state [:z-axis] dec)
    "a" (update-in state [:x-axis] dec)
    "d" (update-in state [:x-axis] inc)
    "q" (update-in state [:y-axis] #(- % 5))
    "e" (update-in state [:y-axis] #(+ % 5))
    state))

(defn display [_ state]
  (rotate (:rot-x state) 1 0 0)
  (rotate (:rot-y state) 0 1 0)
  (translate (:x-axis state) (:y-axis state) (:z-axis state))
  (println (:x-axis state) (:y-axis state) (:z-axis state))
  (let [draw-scene (:scene state)]
       (clear)
       (draw-scene)))

;;tom
;;doesn't work for me.  Probably hardware not supporting the shader pipeline.
(defn start []
  (app/start {:init init, :reshape reshape, :mouse-drag mouse-drag, :display display  :key-press key-press}
{:rot-x 0, :rot-y 0 :x-axis 0 :y-axis 0 :z-axis 0}))

(defn -main
  "Run a simulation for the given height map."
  [map-name & args]
  (start))
