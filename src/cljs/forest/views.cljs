(ns forest.views
  (:require
   [re-frame.core :as re-frame :refer [dispatch]]
   [reagent.core :as reagent]
   [forest.subs :as subs]
   ))

(defn view-port
  ([] (view-port {:id "canvas-container" :width "1280" :height "960" :tabIndex "1"}))
  ([props]
   [:div
    (assoc props :ref #(dispatch [:canvas-loaded %]))]))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [view-port]
     ]))
