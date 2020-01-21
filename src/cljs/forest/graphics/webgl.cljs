(ns forest.graphics.webgl
  (:require [thi.ng.geom.core :as geom]
            [thi.ng.geom.matrix :as mat]

            [thi.ng.geom.gl.core :as gl]
            [thi.ng.geom.gl.camera :as cam]
            [thi.ng.geom.gl.glmesh :as glmesh]
            [thi.ng.geom.gl.webgl.constants :as glc]
            [thi.ng.geom.vector :as v :refer [vec3]]
            [thi.ng.geom.attribs :as attr]

            [thi.ng.geom.aabb :as a]
            [thi.ng.geom.triangle :as tri]
            [thi.ng.geom.circle :as c]

            [forest.graphics.geography :refer [height-map]]
            [forest.graphics.shaders :refer [make-shaders]]
            ))

(defn gl-context [canvas]
  (let [gl-ctx (gl/gl-context canvas)]
    {:gl-ctx gl-ctx
     :canvas canvas
     :camera (cam/perspective-camera {:far 2000
                                      :near 0.1
                                      :eye (vec3 0.0, 200.0, -1500.0)})
     :shaders (make-shaders gl-ctx)}))


(defn points-2d-vertices [points]
  (for [m (range (dec (count points)))
        n (range (dec (count (first points))))]
    [[n m] [n (inc m)] [(inc n) m]
     [(inc n) m]
     [(inc n) (inc m)]
     [n (inc m)]]))


(defn height-point [heights points-per-meter [x y]]
  (let [h-width (/ (count (first height-map)) 2)
        h-height (/ (count height-map) 2)]
    (vec3 (* (- x h-width) points-per-meter)
          (-> heights (nth y) (nth x))
          (* (- y h-height) points-per-meter))))

(defn height-map-points [heights points-per-meter]
  (->> heights
       points-2d-vertices
       (apply concat)
       (map (partial height-point heights points-per-meter))
       (partition 3)))


(defn make-model [height-map]
  (let [faces (height-map-points height-map 10)
        mesh (glmesh/gl-mesh (* 3 (count faces)))]
    (doseq [[id face] (map-indexed vector faces)]
      (geom/add-face mesh (attr/generate-face-attribs face id {} {})))
    mesh))

(defn combine-model-shader-and-camera
  [gl-ctx model shader camera]
  (-> model
      (gl/as-gl-buffer-spec {})
      (assoc :shader shader)
      (gl/make-buffers-in-spec gl-ctx glc/static-draw)
      (cam/apply camera)))

(defn draw-frame!
  [gl-ctx camera shader model]
  (doto gl-ctx
    (gl/clear-color-and-depth-buffer 0 0 0 1 1)
    (gl/draw-with-shader (combine-model-shader-and-camera gl-ctx model shader camera))))
