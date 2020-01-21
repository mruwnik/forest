(ns forest.graphics.shaders
  (:require [thi.ng.geom.core :as geom]
            [thi.ng.geom.gl.shaders :as shaders]))

(def height-shader
  {:vs "void main() {
          scaledHeight = position.y / maxHeight;
          gl_Position = proj * view * vec4(position, 1.0);
       }"
   :fs "void main() {
           gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0) + scaledHeight;
       }"
   :uniforms {:view       :mat4
              :proj       :mat4
              :maxHeight  :float}
   :attribs  {:position   :vec3}
   :apply-model (fn [shader model]
                  (assoc-in shader
                            [:uniforms :maxHeight :default]
                            (->> model (geom/vertices) (map second) (apply max))))
   :varying {:scaledHeight :float}})


(defn make-shaders [gl-ctx]
  {:height (shaders/make-shader-from-spec gl-ctx height-shader)})
