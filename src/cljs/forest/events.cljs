(ns forest.events
  (:require
   [re-frame.core :as re-frame]
   [forest.db :as db]
   [forest.graphics.webgl :as graphics]

   [thi.ng.geom.gl.core :as gl]
   [thi.ng.geom.gl.webgl.constants :as glc]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

;; Input handlers
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
     (-> db
         (update-in [:graphics :state] graphics/set-environment-values (:map-data db))
         (update-in [:graphics :camera]
                    graphics/update-view
                    (offsets key-code [0 0 0]) 0 0)))))


;; Drawing
(re-frame/reg-event-fx
 ::draw!
 (fn [{{{:keys [gl-ctx camera state]} :graphics} :db} [_ a]]
   (when state
     (graphics/draw-frame! gl-ctx camera state))
   nil))

 ;; Redraw the scene every second
(defn dispatch-timer-event []
  (let [now (js/Date.)]
    (re-frame/dispatch [::draw! now])))
(defonce do-timer (js/setInterval dispatch-timer-event (/ 1000 60)))


;; Model handlers
(re-frame/reg-event-db
 ::setup-graphics
 (fn [db [_ [canvas map-data]]]
  (if-not (and map-data canvas)
    db ;; if map-data or canvas are not set, there is nothing to display or set up
    (assoc db :graphics (graphics/setup-context canvas map-data)))))

(re-frame/reg-event-fx
 :canvas-loaded
 (fn [{{map-data :map-data :as db} :db} [_ canvas]]
   {:db (assoc-in db [:graphics :canvas] canvas)
    :dispatch [::setup-graphics [canvas map-data]]}))

(re-frame/reg-event-fx
 ::load-model
 (fn [{{{canvas :canvas} :graphics :as db} :db} [_ map-data]]
   {:db (assoc db :map-data map-data)
    :dispatch [::setup-graphics [canvas map-data]]}))

;; Fetch models
(defn get-model
  [uri]
  (let [xhr (js/XMLHttpRequest.)]
    (set! (.-responseType xhr) "text")
    (set! (.-onload xhr)
          (fn [e]
            (if-let [buf (.-response xhr)]
              (re-frame/dispatch [::load-model (cljs.reader/read-string buf)])
              (prn "error loading model:" (.toString e)))))
    (doto xhr
      (.open "GET" uri true)
      (.send))))

(re-frame/reg-event-fx
 ::get-model
 (fn [db [_ url]] (get-model url) nil))
