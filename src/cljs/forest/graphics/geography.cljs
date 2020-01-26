(ns forest.graphics.geography
  (:require ["pngjs" :refer [PNG]]))


(defn scale-heights-down [data]
  "Scale the given data down. This is done because picture height maps get scaled up so
as to not lose precision when casting to integers."
  (map #(/ % 64) data))

(defn pixels [image]
  "Get all pixels from the given image scaled down."
  (scale-heights-down
   (.getDataElements (-> image .getRaster) 0 0 (.getWidth image) (.getHeight image) nil)))

(defn load-png-height-map [filename]
"Return a height map (in metres) on the basis of the provided png file."
  )
  ;; (let [png (PNG.)]))
  ;; (let [image (ImageIO/read (as-file filename))]
  ;;   (partition (.getWidth image) (pixels image))))

