(ns forest.graphics.webgl
  (:require [thi.ng.geom.core :as geom]
            [thi.ng.geom.quaternion :as q]
            [thi.ng.geom.matrix :as mat]
            [thi.ng.math.core :as m]
            [thi.ng.geom.mesh.io :as mio]

            [thi.ng.geom.gl.core :as gl]
            [thi.ng.geom.gl.camera :as cam]
            [thi.ng.geom.gl.glmesh :as glmesh]
            [thi.ng.geom.gl.webgl.constants :as glc]
            [thi.ng.geom.vector :as v :refer [vec3]]
            [thi.ng.geom.attribs :as attr]
            [forest.graphics.shaders :refer [make-shaders]]
            ))

(defn update-view
  "Update the camera by moving it along the given translation vector or rotating it by `pitch` or `yaw`."
  [{:keys [target eye] :as camera} translate pitch yaw]
  (cond
    (not-every? #{0} [pitch yaw])
    (let [rotation (q/quat-from-euler :xyz yaw pitch 0)
          target (geom/transform-vector rotation target)]
      (cam/set-view camera {:target target}))

    ; This doesn't really work. The idea is to get the current camera rotation, turn the
    ; translation vector (which is a unit one) by the rotation, then add it to both
    ; the `eye` and `target` vectors so as to move them in whatever direction. I'm
    ; guessing my maths is off...
    (not-every? #{0} translate)
    (let [rot (m/normalize (q/quat (:xyz target) (m/mag target)))
          translation (geom/transform-vector rot translate)]
      (cam/set-view camera {:target (m/+ translation target) :eye (m/+ translation eye)}))

    :else camera))

(defn points-2d-vertices [points]
  (for [m (range (dec (count points)))
        n (range (dec (count (first points))))]
    [[n m] [n (inc m)] [(inc n) m]
     [(inc n) m]
     [(inc n) (inc m)]
     [n (inc m)]]))

(defn height-point [map-data points-per-meter [x y]]
  (let [h-width (/ (count (first map-data)) 2)
        h-height (/ (count map-data) 2)]
    (vec3 (* (- x h-width) points-per-meter)
          (-> map-data (nth y) (nth x) :h)
          (* (- y h-height) points-per-meter))))


(defn height-map-points [map-data points-per-meter]
  (->> map-data
       points-2d-vertices
       (apply concat)
       (map (partial height-point map-data points-per-meter))
       (partition 3)))


(defn make-model [map-data]
  (let [faces (height-map-points map-data 50)
        mesh (glmesh/gl-mesh (* 3 (count faces)))]
    (doseq [[id face] (map-indexed vector faces)]
      (geom/add-face mesh (attr/generate-face-attribs face id {} {})))
    mesh))


(defn combine-model-shader
  [gl-ctx model shader]
  (-> model
      (gl/as-gl-buffer-spec {})
      (assoc :shader ((:apply-model shader) shader model))
      (gl/make-buffers-in-spec gl-ctx glc/static-draw)))

(defn draw-frame!
  [gl-ctx camera model]
  (doto gl-ctx
    (gl/clear-color-and-depth-buffer 0 0 0 1 1)
    (gl/draw-with-shader (cam/apply model camera))))


(defn setup-context [canvas map-data]
  (let [gl-ctx (gl/gl-context canvas)
        ; model
        model (make-model map-data)
        ; camera settings
        max-height (->> map-data (map (partial map :h)) flatten (apply max))
        width (count map-data)
        eye (vec3 0.0 (* 2 max-height) (* -32 width))
        ; shaders
        shaders (make-shaders gl-ctx)]
    {:gl-ctx gl-ctx
     :canvas canvas
     :camera (cam/perspective-camera {:far 10000
                                      :near 0.1
                                      :eye eye
                                      :target (m/+ eye (vec3 0.0, 0.0, 1.0))})
     :model model
     :shaders shaders
     :state (combine-model-shader gl-ctx model (:height shaders))
     }))
