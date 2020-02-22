(ns forest.graphics.shaders
  (:require [thi.ng.geom.core :as geom]
            [thi.ng.geom.gl.shaders :as shaders]))

(def height-shader
  {:vs "void main() {
          scaledHeight = position.y / maxHeight;

          if(water > 1.0){
              gl_Position = proj * view * vec4(position.x, position.y + water - 1.0, position.z, 1.0);
              colour = vec4(0.0, 0.0, 1.0, 1.0);
          } else {
              gl_Position = proj * view * vec4(position, 1.0);
              colour = vec4(0.0, 0.0, water, 1.0) + scaledHeight;
          }
       }"
   :fs "void main() {
           gl_FragColor = colour;
       }"
   :uniforms {:view       :mat4
              :proj       :mat4
              :maxHeight  :float}
   :attribs  {:position   :vec3
              :water      :float}
   :apply-model (fn [shader model]
                  (assoc-in shader
                   [:uniforms :maxHeight :default]
                   (:maxHeight model)))
   :varying {:scaledHeight :float :colour :vec4}})


(defn make-shaders [gl-ctx]
  {:height (shaders/make-shader-from-spec gl-ctx height-shader)})
