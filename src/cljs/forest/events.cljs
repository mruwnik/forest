(ns forest.events
  (:require
   [re-frame.core :as re-frame]
   [forest.db :as db]
   [forest.graphics.webgl :as graphics]
   [forest.graphics.geography :refer [height-map]]
   ))

(defn join-model-shader
  "Make sure that all shaders have proper defaults on the basis of the current models."
  [{model :model {:keys [shaders gl-ctx]} :graphics :as db}]
  (->> shaders
       :height
       (graphics/combine-model-shader gl-ctx model)
       (assoc-in db [:graphics :state])))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-fx
 :canvas-loaded
 (fn [{:keys [db]} [_ canvas]]
   (when canvas
     {:db (-> db
              (assoc :graphics (graphics/gl-context canvas))
              join-model-shader)
      :dispatch [::draw! nil]})))

(re-frame/reg-event-db
 ::load-model
 (fn [db [_ model]]
   (-> db
       (assoc :model (graphics/make-model height-map))
       join-model-shader)))


(re-frame/reg-event-db
 :mouse-move
 (fn [db [_ [x y]]]
   (update-in db [:graphics :camera]
              graphics/update-view
              [0 0 0] (/ x 50) (/ y 50))))

(re-frame/reg-event-db
 :key-pressed
 (fn [db [_ key-code]]
   (let [offsets {\w [0 0 1]
                  \s [0 0 -1]
                  \a [-1 0 0]
                  \d [1 0 0]}]
     (update-in db [:graphics :camera]
                graphics/update-view
                (offsets key-code [0 0 0]) 0 0))))

(re-frame/reg-event-fx
 ::draw!
 (fn [{{{:keys [gl-ctx camera state]} :graphics} :db} [_ a]]
   (graphics/draw-frame! gl-ctx camera state)
   nil))


;; Redraw the scene every second
(defn dispatch-timer-event []
  (let [now (js/Date.)]
    (re-frame/dispatch [::draw! now])))
(defonce do-timer (js/setInterval dispatch-timer-event (/ 1000 60)))
