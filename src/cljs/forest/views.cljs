(ns forest.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [forest.subs :as subs]
   ))

(defn view-port
  ([] (view-port {:id "canvas" :width "640" :height "480"}))
  ([props]
   [:canvas (assoc props :ref #(re-frame/dispatch [:canvas-loaded %]))]))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [view-port]
     ]))
