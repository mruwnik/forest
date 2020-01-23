(ns forest.views
  (:require
   [re-frame.core :as re-frame :refer [dispatch]]
   [reagent.core :as reagent]
   [forest.subs :as subs]
   ))

(defn view-port
  ([] (view-port {:id "canvas" :width "640" :height "480"}))
  ([props]
   [:canvas
    (merge
     {:ref #(dispatch [:canvas-loaded %])
      :on-mouse-move #(dispatch [:mouse-move [(.-movementX %) (.-movementY %)]])}
     props)]))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [view-port]
     ]))
