(ns phzr-demo.paint-tiles-demo
  (:require [phzr.core :as p :refer [pset!]]
            [phzr.game :as pg]
            [phzr.game-object-factory :as pgof]
            [phzr.graphics :as pgr]
            [phzr.keyboard :as pk]
            [phzr.loader :as pl]
            [phzr.tilemap :as pt]
            [phzr.tilemap-layer :as ptl]
            [phzr.utils.debug :as pud]))


(enable-console-print!)

(defn ^:private preload
  [game]
  (let [loader (:load game)]
    (doto loader
      (pl/tilemap "desert" "tilemaps/desert.json" nil (pt/const :tiled-json))
      (pl/image "tiles" "images/tmw_desert_spacing.png"))))

(defn ^:private init-tilemap!
  [gof tmap]
  (reset! tmap (pgof/tilemap gof "desert"))
  (pt/add-tileset-image @tmap "Desert" "tiles"))

(defn ^:private init-current-tile!
  [current-tile tmap]
  (reset! current-tile (pt/get-tile @tmap 2 3)))

(defn ^:private init-layer!
  [tmap layer]
  (reset! layer (pt/create-layer @tmap "Ground"))
  (ptl/resize-world @layer))

(defn ^:private init-marker!
  [gof marker]
  (let [initialized-marker (reset! marker (pgof/graphics gof))]
    (doto initialized-marker
      (pgr/line-style 2 0x000000 1)
      (pgr/draw-rect 0 0 32 32))))

(defn ^:private init-cursors!
  [cursors game]
  (reset! cursors (pk/create-cursor-keys (get-in game [:input :keyboard]))))

(defn ^:private build-create-fn
  [current-tile cursors layer marker tmap]
  (fn [game]
    (let [gof (:add game)]
      (init-tilemap! gof tmap)
      (init-current-tile! current-tile tmap)
      (init-layer! tmap layer)
      (init-marker! gof marker)
      (init-cursors! cursors game))))

(defn ^:private update-layer!
  [game layer marker]
  (let [world-x (get-in game [:input :active-pointer :world-x])
        world-y (get-in game [:input :active-pointer :world-y])
        tile-x  (ptl/get-tile-x @layer world-x)
        tile-y  (ptl/get-tile-y @layer world-y)]
    (pset! @marker :x (* tile-x 32))
    (pset! @marker :y (* tile-y 32))))

(defn ^:private update-current-tile!
  [current-tile layer marker tmap]
  (let [tile-x (ptl/get-tile-x @layer (:x @marker))
        tile-y (ptl/get-tile-y @layer (:y @marker))]
    (reset! current-tile (pt/get-tile @tmap tile-x tile-y))))

(defn ^:private draw-tile!
  [current-tile layer marker tmap]
  (let [tile-x (ptl/get-tile-x @layer (:x @marker))
        tile-y (ptl/get-tile-y @layer (:y @marker))]
    (when-not (= @current-tile (pt/get-tile @tmap tile-x tile-y))
      (pt/put-tile @tmap @current-tile tile-x tile-y))))

(defn ^:private handle-draw!
  [current-tile game layer marker tmap]
  (let [pointer-down? (get-in game [:input :mouse-pointer :is-down])
        keyboard      (get-in game [:input :keyboard])
        shift-down?   (pk/is-down keyboard (pk/const :shift))]
    (when pointer-down?
      (if shift-down?
        (update-current-tile! current-tile layer marker tmap)
        (draw-tile! current-tile layer marker tmap)))))

(defn ^:private update-camera!
  [cursors game]
  (cond
    (get-in @cursors [:left :is-down])
    (pset! (:camera game) :x (- (:camera game) 4))

    (get-in @cursors [:right :is-down])
    (pset! (:camera game) :x (+ (:camera game) 4)))

  (cond
    (get-in @cursors [:up :is-down])
    (pset! (:camera game) :y (- (:camera game) 4))

    (get-in @cursors [:down :is-down])
    (pset! (:camera game) :y (+ (:camera game) 4))))

(defn ^:private build-update-fn
  [current-tile cursors layer marker tmap]
  (fn [game]
    (update-layer! game layer marker)
    (handle-draw! current-tile game layer marker tmap)
    (update-camera! cursors game)))

(defn ^:private render
  [game]
  (let [debug (:debug game)]
    (pud/text debug (str "Left-click to paint. Shift + Left-click "
                         "to select tile. Arrows to scroll.")
              32 32
              "#efefef")))

(defn ^:private build-states
  []
  (let [tmap         (atom nil)
        layer        (atom nil)
        marker       (atom nil)
        current-tile (atom nil)
        cursors      (atom nil)
        create       (build-create-fn current-tile cursors layer marker tmap)
        update       (build-update-fn current-tile cursors layer marker tmap)]
    {:preload preload
     :create  create
     :render  render
     :update  update}))

(defn start-demo
  [id]
  (pg/->Game 800 600 (p/phaser-constants :auto) id (build-states)))
