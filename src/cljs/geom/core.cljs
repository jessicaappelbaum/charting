(ns geom.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [thi.ng.geom.viz.core :as viz]
            [thi.ng.geom.svg.core :as svg]
            [thi.ng.geom.core.vector :as v]
            [thi.ng.color.core :as col]
            [thi.ng.math.core :as m :refer [PI TWO_PI]]
            [cljs.pprint]))

;; -------------------------
;; Views
(defn export-viz
  [viz]
  (let [[k m & stuff] (svg/svg {:width 600 :height 320} viz)]
    (vec (concat [k (dissoc m "xmlns:xlink")] stuff))))

(defn bar-spec
  [num width]
  (fn [idx col]
    {:values     (map (fn [i] [i (m/random 100)]) (range 2000 2016))
     :attribs    {:stroke       col
                  :stroke-width (str (dec width) "px")}
     :layout     viz/svg-bar-plot
     :interleave num
     :bar-width  width
     :offset     idx}))

(def viz-spec
  {:x-axis (viz/linear-axis
            {:domain [1999 2016]
             :range  [50 580]
             :major  1
             :pos    280
             :label  (viz/default-svg-label int)})
   :y-axis (viz/linear-axis
            {:domain      [0 100]
             :range       [280 20]
             :major       10
             :minor       5
             :pos         50
             :label-dist  15
             :label-style {:text-anchor "end"}})
   :grid   {:minor-y true}})



(defn home-page []
  [:div [:h2 "Welcome to geom"]
   [:div [:a {:href "/about"} "go to about page"]]
   [:div "seven turtle" ]
   (-> viz-spec
     (assoc :data [((bar-spec 1 20) 0 "#0af")])
     (viz/svg-plot2d-cartesian)
     ((fn [x] (cljs.pprint/pprint x) x))
     (export-viz))])

(defn about-page []
  [:div [:h2 "About geom"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
