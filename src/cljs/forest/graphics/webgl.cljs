(ns forest.graphics.webgl
  (:require [thi.ng.geom.core :as geom]
            [thi.ng.geom.quaternion :as q]
            [thi.ng.geom.matrix :as mat]
            [thi.ng.math.core :as m]
            [thi.ng.geom.gl.arcball :as arc]

            [thi.ng.geom.gl.core :as gl]
            [thi.ng.geom.gl.camera :as cam]
            [thi.ng.geom.gl.glmesh :as glmesh]
            [thi.ng.geom.gl.webgl.constants :as glc]
            [thi.ng.geom.vector :as v :refer [vec3]]
            [thi.ng.geom.attribs :as attr]
            [forest.graphics.geography :refer [height-map]]
            [forest.graphics.shaders :refer [make-shaders]]
            ))

(defn gl-context [canvas]
  (let [gl-ctx (gl/gl-context canvas)]
    {:gl-ctx gl-ctx
     :canvas canvas
     :camera (cam/perspective-camera {:far 2000
                                      :near 0.1
                                      :eye (vec3 0.0, 200.0, -1501.0)
                                      :target (vec3 0.0, 200.0, -1500.0)})
     :shaders (make-shaders gl-ctx)}))

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


(defn combine-model-shader
  [gl-ctx model shader]
  (when gl-ctx
    (-> model
        (gl/as-gl-buffer-spec {})
        (assoc :shader ((:apply-model shader) shader model))
        (gl/make-buffers-in-spec gl-ctx glc/static-draw))))

(defn draw-frame!
  [gl-ctx camera model]
  (doto gl-ctx
    (gl/clear-color-and-depth-buffer 0 0 0 1 1)
    (gl/draw-with-shader (cam/apply model camera))))
