(ns forest.events
  (:require
   [re-frame.core :as re-frame]
   [forest.db :as db]
   [forest.graphics.webgl :as graphics]
   [forest.graphics.geography :refer [height-map]]
   ))

(defn join-model-shader
  "Make sure that all shaders have proper defaults on the basis of the current models."
  [{model :model {shaders :shaders} :graphics :as db}]
  (let [height-shader-applier (or (-> shaders :height :apply-model) first)]
    (-> db
        (update-in [:graphics :shaders :height] height-shader-applier model))))


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


(re-frame/reg-event-fx
 ::draw!
 (fn [{{{:keys [gl-ctx camera shaders]} :graphics model :model} :db} [_ a]]
   (graphics/draw-frame! gl-ctx camera (:height shaders) model)
   {:draw! nil}))


;; Redraw the scene every second
(defn dispatch-timer-event []
  (let [now (js/Date.)]
    (re-frame/dispatch [::draw! now])))
(defonce do-timer (js/setInterval dispatch-timer-event 1000))
