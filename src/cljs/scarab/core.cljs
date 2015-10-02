(ns ^:figwheel-always scarab.core
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom]))

(enable-console-print!)

(defonce app-state (atom {:text "Hello world!"}))

(defn app-component [app owner]
  (reify
    om/IRender
    (render [_] (dom/h1 nil "Om is live!"))))

(defn main []
  (om/root app-component app-state {:target (. js/document (getElementById "app"))}))

(defn on-js-reload []
  (main))
