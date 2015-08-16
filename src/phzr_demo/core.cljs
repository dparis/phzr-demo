(ns ^:figwheel-always phzr-demo.core
    (:require [domina :as d]
              [domina.css :as dc]
              [domina.events :as de]
              [phzr.game :as pg]
              [phzr-demo.animation-demo :as pad]
              [phzr-demo.physics-chain-demo :as pcd])
    (:require-macros [phzr-demo.macros :refer [slurp]]))


(enable-console-print!)

(defonce demo (atom nil))

(defn ^:private init-code-display!
  []
  (.initHighlightingOnLoad js/hljs))

(def ^:private demo-div-id
  "phzr-demo")

(defn ^:private reset-code-display!
  [code]
  (let [code-node (d/by-id "code-display")]
    (d/set-text! code-node code)
    (.highlightBlock js/hljs code-node)))

(def ^:private animation-code
  (slurp "src/phzr_demo/animation_demo.cljs"))

(defn ^:private start-animation-demo!
  []
  (println "Starting animation demo")
  (when-let [cur-demo @demo]
    (pg/destroy cur-demo))
  (reset! demo (pad/start-demo demo-div-id))
  (reset-code-display! animation-code))

(def ^:private physics-chain-code
  (slurp "src/phzr_demo/physics_chain_demo.cljs"))

(defn ^:private start-physics-chain-demo!
  []
  (println "Starting physics chain demo")
  (when-let [cur-demo @demo]
    (pg/destroy cur-demo))
  (reset! demo (pcd/start-demo demo-div-id))
  (reset-code-display! physics-chain-code))

(defn ^:private init-buttons!
  []
  (de/listen! (d/by-id "animation-btn") :click start-animation-demo!)
  (de/listen! (d/by-id "physics-chain-btn") :click start-physics-chain-demo!))

(defn ^:private init-page!
  []
  (println "Setting up UI handlers")
  (init-buttons!))

(init-page!)

(defn on-js-reload
  [])
