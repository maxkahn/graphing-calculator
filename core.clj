(ns graphing.core
  (:gen-class))

(def dictionary
     {"^" "Math/pow" "sin" "Math/sin" "cos" "Math/cos" "tan" "Math/tan"
      "ln" "Math/log" "log" "Math/log10" "e^" "Math/exp" "pi" "Math/PI"})

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn group-terms [s]
        "Adds parens to whitespace-delimited blocks."
        (map #(list (apply str %)) (remove #(= (first %) \space)
                            (partition-by (partial = \space) s))))

(defn replace-ops [s]
      "Finds operators and replaces them with Clojure functions."
     (if (seq? s)
       (if (empty? s)
         s
         (map replace-ops s))
       (if (string? s)
         (let [a (count s)]
           (loop [counterl 0 counterr 1]
             (if (> counterl (dec a))
                 s
                 (if (> counterr a)
                     (recur (inc counterl) (+ 2 counterl))
                     (if (nil? (get dictionary (subs s counterl counterr)))
                         (recur counterl (inc counterr))
                         (str (subs s 0 counterl) " "
                              (get dictionary (subs s counterl counterr))
                              " " (subs s counterr a)))))))
         s)))

(defn number-kludge [coll]
  "Converts number chars to double, other chars to symbols."
       (let [digits #{\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \.}]
         (if (some #{(first coll)} digits)
           (java.lang.Double/parseDouble (apply str coll))
           (symbol (apply str coll)))))

(defn cleanup [s]
        "Takes string with whitespace; returns symbols. s a list containing one string."
        (map number-kludge
             (remove #(= (first %) \space) (partition-by (partial identical? \space) (first s)))))

(defn promote-op [s]
  "s is the whole expression here; promote-op works recursively."
    (if (seq? s)
      (if (empty? s)
        s
        (if (empty? (rest s))
        s
        (if (empty? (rest (rest s)))
          s
          (cons (promote-op (first (rest s))) (map promote-op (cons (first s) (rest (rest s))))))))
      s))

(defn clear-parens [s]
  "Clears redundant parens. Leaves parens around already eval'd expressions."
     (if (seq? s)
       (if (empty? (rest s))
         (first s)
         (map clear-parens s))
       s))

(defn clear-bad-structs [s]
        "s a form. Constants and symbols should not be in op position."
           (if (seq? s)
             (if (ifn? (first s))
               (if (or (= (first s) (symbol "x")) (= (first s) (symbol "y")))
                 (first s)
                 (map clear-bad-structs s))
               (clear-bad-structs (first s)))
             s))

(defmacro defn-special [sym1 sym2]
  "Generates a function of x and y given an expression in canonical form."
    (list* `fn [sym1 sym2] (list 
      (clear-bad-structs 
        (clear-parens (promote-op (map cleanup (replace-ops 
                                                 (read-string (read-line))))))))))


(defn create-axes [panel x1 x2 y1 y2]
        "Adds both axes to a preexisting frame."
        (.add panel (proxy [javax.swing.JComponent] []
                      (paintComponent [g]
                        (let [g2 (cast java.awt.Graphics2D g)
                              line (java.awt.geom.Line2D$Double. x1 y1 x2 y2)
                              line2 (java.awt.geom.Line2D$Double. y1 x1 y2 x2)]
                          (.draw g2 line)
                          (.draw g2 line2)))))
        (.validate panel)
        panel)

(defn create-graph [panel f h]
           (.add panel (proxy [javax.swing.JComponent] []
                         (paintComponent [g]
                           (let [g2 (cast java.awt.Graphics2D g)]
                             (doseq [i (range -2.5 2.5 0.01)
                                     j (range -2.5 2.5 0.01)
                                     :when (<= (Math/abs (- (f i j) (h i j))) 0.01)]
                               (.draw g2 (java.awt.geom.Line2D$Double. (+ 250 (* 100 i)) (- 250 (* 100 j)) 
                                                                       (+ 250 (* 100 i)) (- 250 (* 100 j)))))))))
                          (.validate panel))
  
(def my-panel (javax.swing.JPanel. ))

(def my-frame (doto (javax.swing.JFrame. )
     (.setSize 500 500)
     (.setTitle "My Graph")
     (.setDefaultCloseOperation javax.swing.JFrame/EXIT_ON_CLOSE)))


(defn -main [& args]
  
  
  (.setLayout my-panel (javax.swing.OverlayLayout. my-panel))
  
  
  (create-axes my-panel 0 499 250 250)
  
  (create-graph my-panel (defn-special x y) (defn-special x y))
  
  (.add my-frame my-panel)
  
  (.setVisible my-frame true))
