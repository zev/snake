(ns snake.core
  (:require [domina :as dm]
            [domina.events :as de]
            [domina.css :as dc]))

(enable-console-print!)

;;(println "Hello world!")

(def board  (dm/by-id "board"))
(def canvas (.getContext board "2d"))
(def board-color "rgb(220,220,220)")

(def width 75)
(def height 50)
(def point-size 10)
(def turn-millis 100)
(def win-length 15)
(def dirs {:left [-1 0]
           :right [ 1 0]
           :up [0 -1]
           :down [ 0 1]})

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

(defn out-of-bounds?
  "Check if the head has hit the borders of the game board"
  [{[[x y] & body] :body}]
  (or (< x 0)
      (< y 0)
      (> x width)
      (> y height)))

(defn lose?
  [snake]
  (some #(% snake) [head-overlaps-body? out-of-bounds?]))

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
  "Updates the snake and apple atoms with new state changes.
   Returns the locations where data has changed (optimization for redrawing)"
  [snake apple]
  (let [tail (last (:body @snake))]
    (if (eats? @snake @apple)
      (do (reset! apple (create-apple))
          (swap! snake move :grow)
          [(first (:body @snake))])
      (do (swap! snake move)
          [(first (:body @snake)) tail]
          ))))


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
     (aset g "fillStyle" board-color)
     (.fillRect g 0 0 (.-width g) (.-height g))
     ;; (.save g)
     )
  ([g restore]
     (.restore g)
     (.save g)))

(defn paint-canvas
  [g snake apple changes]
  ;; (draw-board g)
  (doseq [point changes]
    (fill-point g point board-color))
  (paint g @snake changes)
  (paint g @apple))

(defn set-canvas-size
  [canvas]
  (let [nwidth (* (inc width) point-size)
        nheight (* (inc height) point-size)]
  (set! (.-width canvas) nwidth)
  (set! (.-height canvas) nheight)))

(defn prepare-game
  [snake apple]
  (draw-board canvas)
  (reset-game snake apple))

(defn iterate-game
  [snake apple]
  (let [changes (update-positions snake apple)]
    (when (lose? @snake)
      (js/alert "You lose!")
      (prepare-game snake apple))
    (when (win? @snake)
      (js/alert "You win!")
      (prepare-game snake apple))
    (paint-canvas canvas snake apple changes)))

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
    (de/listen! (dc/sel "body") :keydown (fn [e] (key-pressed e snake)))
    (prepare-game snake apple)
    (iterate-game snake apple)
    (js/setInterval (fn []
                      (iterate-game snake apple)) turn-millis)))


(set! (.-onload js/window) game)
