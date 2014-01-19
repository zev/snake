(ns snake.core
  (:require [domina :as dm]
            [domina.events :as de]
            [domina.css :as dc]
            ))

(enable-console-print!)

(println "Hello world!")

(def board  (dm/by-id "board"))
(def canvas (.getContext board "2d"))

(def width 75)
(def height 50)
(def point-size 10)
(def turn-millis 75)
(def win-length 15)
(def dirs {:left [-1 0]
           :right [ 1 0]
           :up [0 -1]
           :down [ 0 1]
           })

(defn add-points
  [& pts]
  (vec (apply map + pts)))

(defn point-to-screen-rect
  [pt]
  (map #(* point-size %)
       [(pt 0) (pt 1) 1 1]))

(defn create-apple
  []
  {:location [(rand-int width) (rand-int height)]
   :color "rgb(210, 50, 90)"
   :type :apple})

(defn create-snake
  []
  {:body (list [1 1])
   :dir [1 0]
   :type :snake
   :color "rgb(15, 160, 70)"})

(defn move
  [{:keys [body dir] :as snake} & grow]
  (assoc snake :body (cons (add-points (first body) dir)
                           (if grow body (butlast body)))))


(defn win?
  [{body :body}]
  (>= (count body) win-length))

(defn head-overlaps-body?
  [{[head & body] :body}]
  (contains? (set body) head))


(def lose? head-overlaps-body?)

(defn turn
  [snake newdir]
  (assoc snake :dir newdir))

(defn reset-game
  [snake apple]
  (reset! apple (create-apple))
  (reset! snake (create-snake))
  nil)

(defn update-direction
  [snake newdir]
  (when newdir
    (swap! snake turn newdir)))

(defn eats?
  [{[snake-head] :body}
   {apple :location}]
  (= snake-head apple))

(defn update-positions
  [snake apple]
  (if (eats? @snake @apple)
    (do (reset! apple (create-apple))
        (swap! snake move :grow))
    (swap! snake move))
  nil)


(defn fill-point
  [g pt color]
  (let [[x y width height] (point-to-screen-rect pt)]
    (set! (.-fillStyle  g) color)
    (.fillRect g x y width height)))

(defmulti paint (fn [g object & _] (:type object)))

(defmethod paint :apple
  [g {:keys [location color]}]
  (fill-point g location color))

(defmethod paint :snake
  [g {:keys [body color]}]
  (doseq [point body]
    (fill-point g point color)))

(defn draw-board
  ([g]
     ;;fill screen
     (aset g "fillStyle" "rgb(220,220,220)")
     (.fillRect g 0 0 (.-width g) (.-height g))
     ;; (.save g)
     )
  ([g restore]
     (.restore g)
     (.save g)))

(defn paint-canvas
  [g snake apple]
  (draw-board g)
  (paint g @snake)
  (paint g @apple))

(defn set-canvas-size
  [canvas]
  (let [nwidth (* (inc width) point-size)
        nheight (* (inc height) point-size)]
  (dm/log "New width " nwidth " new height " nheight)
  (set! (.-width canvas) nwidth)
  (set! (.-height canvas) nheight)))

(defn iterate-game
  [snake apple]

  (update-positions snake apple)
  (when (lose? @snake)
    (reset-game snake apple)
    (js/alert "You lose!"))
  (when (win? @snake)
    (reset-game snake apple)
    (js/alert "You win!"))
  (paint-canvas canvas snake apple))

(def arrow-keys {37 :left
                 38 :up
                 39 :right
                 40 :down})

(defn key-pressed
  [event snake]
  (let [char-code (:charCode event)
        key-code (:keyCode event)
        my-code (if (= 0 key-code) char-code key-code)]

  (update-direction snake (dirs (arrow-keys my-code)))))


(defn game
  []
  (let [snake (atom (create-snake))
        apple (atom (create-apple))]

    (set-canvas-size canvas)
    (draw-board canvas)
    (de/listen! (dc/sel "body") :keydown (fn [e] (key-pressed e snake)))
    (reset-game snake apple)
    (iterate-game snake apple)
    (js/setInterval (fn []
                      (iterate-game snake apple)) turn-millis)))


(set! (.-onload js/window) game)
