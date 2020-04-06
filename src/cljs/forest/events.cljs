(ns forest.events
  (:require
   [re-frame.core :as re-frame]
   [forest.db :as db]
   [forest.graphics.quil :as q]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 :canvas-loaded
 (fn [db [_ canvas]]
   (merge db {:graphics {:sketch (q/start-drawing "canvas-container")}
              :world {:n 100 :m 100
                      :grid (for [y (range 100)]
                              (for [x (range 100)]
                                {:water 0 :plants {}}))}})))

;; (re-frame/reg-event-fx
;;  ::load-model
;;  (fn [{{{canvas :canvas} :graphics :as db} :db} [_ map-data]]
;;    {:db (assoc db :map-data map-data)
;;     :dispatch [::setup-graphics [canvas map-data]]}))

(when nil
  (do
    (re-frame/reg-event-fx
     ::debugger
     (fn [{{map-data :map-data {:keys [model canvas gl-ctx shaders state]} :graphics :as db} :db} [_ e]]
       (println "asd")
       ;; (println (-> state :attribs :position :data (.-length))
       ;; (println (.getAttributeLocation gl-ctx (-> shaders :height :program) "colour"))
       ;; (println (gl/make-array-buffer gl-ctx glc/array-buffer glc/dynamic-draw nil))
       ))
    (re-frame/dispatch [::debugger "asdad"])
    )
  )
