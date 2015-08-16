(ns phzr-demo.physics-chain-demo
  (:require [phzr.core :as p :refer [pset!]]
            [phzr.game :as pg]
            [phzr.game-object-factory :as pgof]
            [phzr.loader :as pl]
            [phzr.physics :as pp]
            [phzr.physics.p2 :as pp2]
            [phzr.physics.p2.body :as pp2b]
            [phzr.sprite :as ps]))

(defn ^:private preload
  [game]
  (let [loader (:load game)]
    (doto loader
        (pl/image "clouds" "images/clouds.jpg")
        (pl/spritesheet "chain" "images/chain.png" 16 26))))

(defn ^:private add-link-sprite
  [game x y width height frame]
  (let [gof  (:add game)
        rect (pgof/sprite gof x y "chain" frame)
        p2   (get-in game [:physics :p2])]
    (pp2/enable p2 rect false)
    (pp2b/set-rectangle (:body rect) width height)
    rect))

(defn ^:private create-rope
  [game length x-anchor y-anchor]
  (let [height    20
        width     16
        max-force 20000
        p2        (get-in game [:physics :p2])]
    (loop [n         0
           prev-link nil]
      (when (< n length)
        (let [x        x-anchor
              y        (+ y-anchor (* n height))
              frame    (mod n 2)
              cur-link (add-link-sprite game x y width height frame)]
          ;; Bring previous link to the top of the z-order
          (when prev-link
            (ps/bring-to-top prev-link)
            (pp2/create-revolute-constraint p2
                                            cur-link  [0 -10]
                                            prev-link [0 10]
                                            max-force))

          ;; Anchor the first link, set up the rest appropriately
          (if (= n 0)
            (pset! (:body cur-link) :static true)
            (do
              (pset! (get-in cur-link [:body :velocity]) :x 400)
              (pset! (:body cur-link) :mass (/ length n))))


          ;; Some kind of problem happening in recursion
          (recur (inc n) cur-link))))))

(defn ^:private create
  [game]
  (let [gof  (:add game)
        phys (:physics game)]
    (pgof/tile-sprite gof 0 0 800 600 "clouds")
    (pp/start-system phys (pp/const :p2-js))
    (pset! (get-in phys [:p2 :gravity]) :y 1200)
    (create-rope game 40 400 64)))

(defn start-demo
  [id]
  (pg/->Game 800 600 (p/phaser-constants :auto) id
             {"preload" preload
              "create"  create}))
