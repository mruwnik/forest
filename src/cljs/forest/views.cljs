(ns forest.views
  (:require
   [re-frame.core :as re-frame :refer [dispatch]]
   [reagent.core :as reagent]
   [forest.subs :as subs]
   ))

(defn view-port
  ([] (view-port {:id "canvas" :width "1280" :height "960" :tabIndex "1"}))
  ([props]
   [:canvas
    (merge
     {:ref #(dispatch [:canvas-loaded %])
      :on-mouse-move #(dispatch [:mouse-move [(.-movementX %) (.-movementY %)]])
      :on-key-down #(dispatch [:key-pressed (.-key %)])
      }
     props)]))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [view-port]
     ]))
