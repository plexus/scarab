(ns ^:figwheel-always scarab.core
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom]
              [clojure.browser.net :as net]
              [clojure.browser.event :as event]
              [cljs.core.async :as csp]
              [cognitect.transit :as transit]
              [sablono.core :as html :refer-macros [html]])
    (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(def app-state-init {:page :ls
                     :repo "ClojureBridge-organizing"
                     :file-list []})

(def app-state (atom app-state-init))

(defn response-text [evt]
  (.getResponseText (.-target evt)))

(def json-reader (transit/reader :json))

(defn response-json [evt]
  (transit/read json-reader (response-text evt)))

(defn http-get [uri f]
  (let [conn (net/xhr-connection)]
    (event/listen conn :complete f)
    (net/transmit conn uri)))


(defn visit-file! [repo name]
  (http-get
   (str "/repo/" (:repo @app-state) "/commit/~/files/" name)
   (fn [evt] (swap! app-state #(merge % {:page :doc, :doc (response-json evt)})))))

(defn file-list-component [data owner]
  (reify
    om/IRender
    (render [_]
      (let [files (:file-list data)
            repo (:repo data)]
        (html
         [:div
          [:h1 "Om is live!"]
          [:ul (map #(vector :li {:onClick (fn [e] (visit-file! repo %))} %)
                    files)]])))))

(declare render-tag)

(defn tag-component [data owner]
  (reify
    om/IRender
    (render [_] (html
                 [(data "tag") (or (data "attrs") {}) (map render-tag (data "content") (range))] ))))

;; #_[(:tag data) (apply str (:content data))]

(defn render-tag [data idx]
  (cond
    (string? data) data
    (vector? data) (map render-tag data (range))
    (map? data) (om/build tag-component data {:react-key idx})))

(defn doc-component [data owner]
  (reify
    om/IRender
    (render [_] (html
                 [:div
                  [:div (map render-tag (:doc data) (range))]
                  #_[:div (pr-str (:doc data))]]))))

(defn app-component [data owner]
  (reify
    om/IRender
    (render [_]
      (case (:page data)
        :ls (om/build file-list-component data)
        :doc (om/build doc-component data)))))

(defn fetch-file-list! [repo]
  (http-get
   (str "/repo/" (:repo @app-state) "/commit/~/ls")
   (fn [evt] (swap! app-state #(assoc % :file-list (response-json evt))))))

(defn main []
  (om/root app-component app-state {:target (. js/document (getElementById "app"))})
  (fetch-file-list! (:repo @app-state)))

(defn on-js-reload [])

(main)
