(ns forest.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub ::name (fn [db] (:name db)))
(re-frame/reg-sub ::world (fn [db] (-> db :world :grid)))
