(ns phzr-demo.animation-demo
  (:require [phzr.core :as p :refer [pset!]]
            [phzr.animation-manager :as pam]
            [phzr.game :as pg]
            [phzr.game-object-factory :as pgof]
            [phzr.loader :as pl]
            [phzr.point :as ppo]
            [phzr.signal :as psg]
            [phzr.sprite :as ps]))

(defn ^:private preload
  [game]
  (let [loader (:load game)]
    (doto loader
      (pl/spritesheet "mummy" "images/metalslug_mummy37x45.png" 37 45 18)
      (pl/spritesheet "monster" "images/metalslug_monster39x40.png" 39 40))))

(defn ^:private change-sprite
  [pointer event sprite]
  (if (= "monster" (:key sprite))
    (ps/load-texture sprite "mummy" 0 false)
    (ps/load-texture sprite "monster" 0 false)))

(defn ^:private create
  [game]
  (let [gof    (:add game)
        sprite (pgof/sprite gof 300 200 "monster")]
    (pam/add (:animations sprite) "walk" (range 0 16))
    (pam/play (:animations sprite) "walk" 20 true)
    (ppo/set (:scale sprite) 6)
    (pset! sprite :smoothed false)

    (psg/add (get-in game [:input :on-down])
             change-sprite nil 0
             sprite)))

(defn start-demo
  [id]
  (pg/->Game 800 600 (p/phaser-constants :auto) id
             {"preload" preload
              "create"  create}))
