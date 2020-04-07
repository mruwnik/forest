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
              ;; set the world staticlly for now, until the graphics work properly
              :world {:grid (for [y (range 20)]
                              (for [x (range 20)]
                                {:water (rand-int 255)
                                 :plants {:grass (rand-int 100) :daisies (rand-int 99)}}))}})))
