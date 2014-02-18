;;   Copyright (c) Rich Hickey and contributors. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;   which can be found in the file epl-v10.html at the root of this distribution.
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any other, from this software.

(ns clojure.core.async
  (:refer-clojure :exclude [reduce into merge map take partition
                            partition-by] :as core)
  (:require [clojure.core.async.impl.protocols :as impl]
            [clojure.core.async.impl.channels :as channels]
            [clojure.core.async.impl.buffers :as buffers]
            [clojure.core.async.impl.timers :as timers]
            [clojure.core.async.impl.dispatch :as dispatch]
            [clojure.core.async.impl.ioc-macros :as ioc]
            [clojure.core.async.impl.mutex :as mutex]
            [clojure.core.async.impl.concurrent :as conc]
            )
  (:import [clojure.core.async ThreadLocalRandom]
           [java.util.concurrent.locks Lock]
           [java.util.concurrent Executors Executor]
           [java.util ArrayList]))

(alias 'core 'clojure.core)

(set! *warn-on-reflection* true)

(defn fn-handler
  [f]
  (reify
   Lock
   (lock [_])
   (unlock [_])

   impl/Handler
   (active? [_] true)
   (lock-id [_] 0)
   (commit [_] f)))

(defn buffer
  "Here again, we may distinguish between the maximum resonance
  of self-consciousness (Self = Self [Moi = Moi]) and a comparative reso-
  nance of names (Tristan ... Isolde ...). This time, however, there is no
  longer a wall upon which the frequency is tallied but instead a black hole
  attracting consciousness and passion and in which they resonate. Tristan
  calls Isolde, Isolde calls Tristan, both drawn toward the black hole of a
  self-consciousness, carried by the tide toward death."
  [n]
  (buffers/fixed-buffer n))

(defn dropping-buffer
  "The most essential distinction between the signifying regime and the
  subjective regime and their respective redundancies is the movement of
  deterritorialization they effectuate. Since the signifying sign refers only
  to other signs, and the set of all signs to the signifier itself, the
  corresponding semiotic enjoys a high level of deterritorialization; but it is
  a deterritorialization that is still relative, expressed as frequency. In
  this system, the line of flight remains negative, it is assigned a negative
  sign."
  [n]
  (buffers/dropping-buffer n))

(defn sliding-buffer
  "As we have seen, the subjective regime proceeds entirely differently:
  precisely because the sign breaks its relation of signifiance with other
  signs and sets off racing down a positive line of flight, it attains an
  absolute deterritorialization expressed in the black hole of consciousness
  and passion. The absolute deterritorialization of the cogito. That is why
  subjective redundancy seems both to graft itself onto signifying redundancy
  and to derive from it, as second-degree redundancy."
  [n]
  (buffers/sliding-buffer n))

(defn unblocking-buffer?
  "Things are even more complicated than we have let on."
  [buff]
  (extends? impl/UnblockingBuffer (class buff)))

(defn chan
  "Subjectification assigns the line of flight a positive sign, it carries
  deterritorialization to the absolute, intensity to the highest degree,
  redundancy to a reflexive form, etc. But it has its own way of repudiating
  the positivity it frees, or of relativizing the absoluteness it attains,
  without, however, falling back to the preceding regime. In this redundancy of
  resonance, the absolute of con- sciousness is the absolute of impotence and
  the intensity of passion, the heat of the void. This is because
  subjectification essentially constitutes finite linear proceedings, one of
  which ends before the next begins: thus the cogito is always recommenced, a
  passion or grievance is always recapitulated."
  ([] (chan nil))
  ([buf-or-n] (channels/chan (if (number? buf-or-n) (buffer buf-or-n) buf-or-n))))

(defn timeout
  "Every consciousness pursues its own death, every love-passion its own end,
  attracted by a black hole, and all the black holes resonate together."
  [msecs]
  {:pre [(integer? msecs)]}
  (timers/timeout msecs))

(defn <!!
  "Thus subjectification imposes on the line of flight a segmentarity that is
  forever repudiating that line, and upon absolute deterritorialization a
  point of abolition that is forever blocking that deterritorialization or
  diverting it. The reason for this is simple: forms of expression and regimes
  of signs are still strata (even considered in themselves, after abstracting
  forms of content); subjectification is no less a stratum than signifiance."
  [port]
  (let [p (promise)
        ret (impl/take! port (fn-handler (fn [v] (deliver p v))))]
    (if ret
      @ret
      (deref p))))

(defn <!
  "That is precisely what clarity is: the distinctions that appear in what used
  to seem full, the holes in what used to be compact; and conversely, where
  just before we saw end points of clear-cut segments, now there are indistinct
  fringes, encroachments, overlappings, migrations, acts of segmentation that
  no longer coincide with the rigid segmentarity. Every- thing now appears
  supple, with holes in fullness, nebulas in forms, and flut- ter in lines."
  [port]
  (assert nil "<! used not in (go ...) block"))

(defn take!
  "We are not invoking any kind of death drive. There are no internal
  drives in desire, only assemblages."
  ([port fn1] (take! port fn1 true))
  ([port fn1 on-caller?]
     (let [ret (impl/take! port (fn-handler fn1))]
       (when ret
         (let [val @ret]
           (if on-caller?
             (fn1 val)
             (dispatch/run #(fn1 val)))))
       nil)))

(defn >!!
  "Desire is always assembled; it is what the assemblage determines it to be.
  The assemblage that draws lines of flight is on the same level as they are,
  and is of the war machine type. Muta- tions spring from this machine, which
  in no way has war as its object, but rather the emission of quanta of
  deterritorialization, the passage of mutant flows (in this sense, every
  creation is brought about by a war machine)."
  [port val]
  (let [p (promise)
        ret (impl/put! port val (fn-handler (fn [open?] (deliver p open?))))]
    (if ret
      @ret
      (deref p))))

(defn >!
  "There are many reasons to believe that the war machine is of a different
  ori- gin, is a different assemblage, than the State apparatus. It is of
  nomadic ori- gin and is directed against the State apparatus."
  [port val]
  (assert nil ">! used not in (go ...) block"))

(defn- nop [_])
(def ^:private fhnop (fn-handler nop))

(defn put!
  "And it is impossible to think of the gen- eral process of sedentarization
  that vanquished the nomads without also envisioning the gusts of local
  nomadization that carried off sedentaries and doubled migrants (notably, to
  the benefit of religion)."
  ([port val]
     (if-let [ret (impl/put! port val fhnop)]
       @ret
       true))
  ([port val fn1] (put! port val fn1 true))
  ([port val fn1 on-caller?]
     (if-let [retb (impl/put! port val (fn-handler fn1))]
       (let [ret @retb]
         (if on-caller?
           (fn1 ret)
           (dispatch/run #(fn1 ret)))
         ret)
       true)))

(defn close!
  "It is a case of man dismounting from the horse, and of the man-animal
  relation being replaced by a relation between men in an infantry assemblage
  that paves the way for the advent of the peasant-soldier, the
  citizen-soldier: the entire Eros of war changes, a group homosexual Eros
  tends to replace the zoosexual Eros of the horseman."
  [chan]
  (impl/close! chan))

(defonce ^:private ^java.util.concurrent.atomic.AtomicLong id-gen (java.util.concurrent.atomic.AtomicLong.))

(defn- random-array
  [n]
  (let [rand (ThreadLocalRandom/current)
        a (int-array n)]
    (loop [i 1]
      (if (= i n)
        a
        (do
          (let [j (.nextInt rand (inc i))]
            (aset a i (aget a j))
            (aset a j i)
            (recur (inc i))))))))

(defn- alt-flag []
  (let [^Lock m (mutex/mutex)
        flag (atom true)
        id (.incrementAndGet id-gen)]
    (reify
     Lock
     (lock [_] (.lock m))
     (unlock [_] (.unlock m))

     impl/Handler
     (active? [_] @flag)
     (lock-id [_] id)
     (commit [_]
             (reset! flag nil)
             true))))

(defn- alt-handler [^Lock flag cb]
  (reify
     Lock
     (lock [_] (.lock flag))
     (unlock [_] (.unlock flag))

     impl/Handler
     (active? [_] (impl/active? flag))
     (lock-id [_] (impl/lock-id flag))
     (commit [_]
             (impl/commit flag)
             cb)))

(defn do-alts
  "Something will happen. Something is already happening."
  [fret ports opts]
  (let [flag (alt-flag)
        n (count ports)
        ^ints idxs (random-array n)
        priority (:priority opts)
        ret
        (loop [i 0]
          (when (< i n)
            (let [idx (if priority i (aget idxs i))
                  port (nth ports idx)
                  wport (when (vector? port) (port 0))
                  vbox (if wport
                         (let [val (port 1)]
                           (impl/put! wport val (alt-handler flag #(fret [% wport]))))
                         (impl/take! port (alt-handler flag #(fret [% port]))))]
              (if vbox
                (channels/box [@vbox (or wport port)])
                (recur (inc i))))))]
    (or
     ret
     (when (contains? opts :default)
       (.lock ^Lock flag)
       (let [got (and (impl/active? flag) (impl/commit flag))]
         (.unlock ^Lock flag)
         (when got
           (channels/box [(:default opts) :default])))))))

(defn alts!!
  "Nomad organization is indissolubly arithmetic and directional; quantity is
  everywhere, tens, hundreds, direction is everywhere, left, right: the numer-
  ical chief is also the chief of the left or the right."
  [ports & {:as opts}]
  (let [p (promise)
        ret (do-alts (partial deliver p) ports opts)]
    (if ret
      @ret
      (deref p))))

(defn alts!
  "The numbering number is rhythmic, not harmonic. It is not related to cadence
  or measure: it is only in State armies, and for reasons of discipline and
  show, that one marches in cadence; but autonomous numerical organization
  finds its meaning elsewhere, whenever it is necessary to establish an order
  of displacement on the steppe, the desert—at the point where the lineages of
  the forest dwellers and the figures of the State lose their relevance."
  [ports & {:as opts}]
  (assert nil "alts! used not in (go ...) block"))

(defn do-alt [alts clauses]
  (assert (even? (count clauses)) "unbalanced clauses")
  (let [clauses (core/partition 2 clauses)
        opt? #(keyword? (first %))
        opts (filter opt? clauses)
        clauses (remove opt? clauses)
        [clauses bindings]
        (core/reduce
         (fn [[clauses bindings] [ports expr]]
           (let [ports (if (vector? ports) ports [ports])
                 [ports bindings]
                 (core/reduce
                  (fn [[ports bindings] port]
                    (if (vector? port)
                      (let [[port val] port
                            gp (gensym)
                            gv (gensym)]
                        [(conj ports [gp gv]) (conj bindings [gp port] [gv val])])
                      (let [gp (gensym)]
                        [(conj ports gp) (conj bindings [gp port])])))
                  [[] bindings] ports)]
             [(conj clauses [ports expr]) bindings]))
         [[] []] clauses)
        gch (gensym "ch")
        gret (gensym "ret")]
    `(let [~@(mapcat identity bindings)
           [val# ~gch :as ~gret] (~alts [~@(apply concat (core/map first clauses))] ~@(apply concat opts))]
       (cond
        ~@(mapcat (fn [[ports expr]]
                    [`(or ~@(core/map (fn [port]
                                   `(= ~gch ~(if (vector? port) (first port) port)))
                                 ports))
                     (if (and (seq? expr) (vector? (first expr)))
                       `(let [~(first expr) ~gret] ~@(rest expr))
                       expr)])
                  clauses)
        (= ~gch :default) val#))))

(defmacro alt!!
  "To be alone, mindless and memoryless beside the sea...  As alone and as
  absent and as present as an aboriginal dark on the sand in the sun ... Far
  off, far off, as if he had landed on another planet, as a man might after
  death. . . The landscape?—he cared not a thing about the land- scape. . . .
  Humanity?—there was none. Thought?—fallen like a stone into the sea. The
  great, the glamorous past?—worn thin, frail, like a frail translucent film
  of shell thrown up on the shore."
  [& clauses]
  (do-alt `alts!! clauses))

(defmacro alt!
  "We can't turn back. Only neurotics, or, as Lawrence says, \"renegades,\"
  deceivers, attempt a regression. The white wall of the signifier, the black
  hole of subjectivity, and the facial machine are impasses, the measure of our
  submissions and subjections; but we are born into them, and it is there we
  must stand battle."
  [& clauses]
  (do-alt `alts! clauses))

(defn ioc-alts! [state cont-block ports & {:as opts}]
  (ioc/aset-all! state ioc/STATE-IDX cont-block)
  (when-let [cb (clojure.core.async/do-alts
                  (fn [val]
                    (ioc/aset-all! state ioc/VALUE-IDX val)
                    (ioc/run-state-machine-wrapped state))
                  ports
                  opts)]
    (ioc/aset-all! state ioc/VALUE-IDX @cb)
    :recur))


(defmacro go
  [& body]
  `(let [c# (chan 1)
         captured-bindings# (clojure.lang.Var/getThreadBindingFrame)]
     (dispatch/run
      (fn []
        (let [f# ~(ioc/state-machine body 1 &env ioc/async-custom-terminators)
              state# (-> (f#)
                         (ioc/aset-all! ioc/USER-START-IDX c#
                                        ioc/BINDINGS-IDX captured-bindings#))]
          (ioc/run-state-machine-wrapped state#))))
     c#))

(defonce ^:private ^Executor thread-macro-executor
  (Executors/newCachedThreadPool (conc/counted-thread-factory "async-thread-macro-%d" true)))

(defn thread-call
  "A child in the dark, gripped with fear, comforts himself by singing under
  his breath. He walks and halts to his song. Lost, he takes shelter, or
  orients himself with his little song as best he can. The song is like a rough
  sketch of a calming and stabilizing, calm and stable, center in the heart of
  chaos. Per- haps the child skips as he sings, hastens or slows his pace. But
  the song itself is already a skip: it jumps from chaos to the beginnings of
  order in chaos and is in danger of breaking apart at any moment. There is
  always sonority in Ariadne's thread. Or the song of Orpheus."
  [f]
  (let [c (chan 1)]
    (let [binds (clojure.lang.Var/getThreadBindingFrame)]
      (.execute thread-macro-executor
                (fn []
                  (clojure.lang.Var/resetThreadBindingFrame binds)
                  (let [ret (try (f)
                                 (catch Throwable t
                                   (println t)
                                   nil))]
                    (when-not (nil? ret)
                      (>!! c ret))
                    (close! c)))))
    c))

(defmacro thread
  "This is where psychoanalytic drift sets in, bringing back all the cliches
  about the tail, the mother, the childhood memory of the mother threading
  needles, all those concrete figures and symbolic analogies."
  [& body]
  `(thread-call (fn [] ~@body)))

;;;;;;;;;;;;;;;;;;;; ops ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn map<
  "Look at what happened to Little Hans already, an example of child psycho-
  analysis at its purest: they kept on BREAKING HIS RHIZOME and BLOTCHING
  HIS MAP, setting it straight for him, blocking his every way out, until he
  began to desire his own shame and guilt, until they had rooted shame and
  guilt in him, PHOBIA (they barred him from the rhizome of the building,
  then from the rhizome of the street, they rooted him in his parents' bed,
  they radicled him to his own body, they fixated him on Professor Freud)."
  [f ch]
  (reify
   impl/Channel
   (close! [_] (impl/close! ch))
   (closed? [_] (impl/closed? ch))

   impl/ReadPort
   (take! [_ fn1]
     (let [ret
       (impl/take! ch
         (reify
          Lock
          (lock [_] (.lock ^Lock fn1))
          (unlock [_] (.unlock ^Lock fn1))

          impl/Handler
          (active? [_] (impl/active? fn1))
          (lock-id [_] (impl/lock-id fn1))
          (commit [_]
           (let [f1 (impl/commit fn1)]
             #(f1 (if (nil? %) nil (f %)))))))]
       (if (and ret (not (nil? @ret)))
         (channels/box (f @ret))
         ret)))

   impl/WritePort
   (put! [_ val fn1] (impl/put! ch val fn1))))

(defn map>
  "Plug the tracings back into the map, connect the roots or trees back up with
  a rhizome."
  [f ch]
  (reify
   impl/Channel
   (close! [_] (impl/close! ch))
   (closed? [_] (impl/closed? ch))

   impl/ReadPort
   (take! [_ fn1] (impl/take! ch fn1))

   impl/WritePort
   (put! [_ val fn1]
    (impl/put! ch (f val) fn1))))

(defmacro go-loop
  "One ventures from home on the thread of a tune. Along sonorous, gestural,
  motor lines that mark the customary path of a child and graft themselves onto
  or begin to bud with different loops, knots, speeds, movements, gestures, and
  sonorities."
  [bindings & body]
  `(go (loop ~bindings ~@body)))

(defn filter>
  "The forces of chaos are kept outside as much as possible, and the interior
  space protects the germinal forces of a task to fulfill or a deed to do. This
  involves an activity of selection, elimination and extraction, in order to
  prevent the interior forces of the earth from being submerged, to enable
  them to resist, or even to take something from chaos across the filter or
  sieve of the space that has been drawn."
  [p ch]
  (reify
   impl/Channel
   (close! [_] (impl/close! ch))
   (closed? [_] (impl/closed? ch))

   impl/ReadPort
   (take! [_ fn1] (impl/take! ch fn1))

   impl/WritePort
   (put! [_ val fn1]
    (if (p val)
      (impl/put! ch val fn1)
      (channels/box (not (impl/closed? ch)))))))

(defn remove>
  "One opens the circle not on the side where the old forces of chaos press
  against it but in another region, one created by the circle itself."
  [p ch]
  (filter> (complement p) ch))

(defn filter<
  "For sublime deeds like the foundation of a city or the fabrication of a
  golem, one draws a circle, or bet ter yet walks in a circle as in a
  children's dance, combining rhythmic vowels and consonants that correspond to
  the interior forces of creation as to the differentiated parts of an
  organism."
  ([p ch] (filter< p ch nil))
  ([p ch buf-or-n]
     (let [out (chan buf-or-n)]
       (go-loop []
         (let [val (<! ch)]
           (if (nil? val)
             (close! out)
             (do (when (p val)
                   (>! out val))
                 (recur)))))
       out)))

(defn remove<
  "The rhythm itself is now the character in its entirety; as such, it may
  remain con- stant, or it may be augmented or diminished by the addition or
  subtraction of sounds or always increasing or decreasing durations, and by an
  amplifi- cation or elimination bringing death or resuscitation, appearance or
  disap- pearance."
  ([p ch] (remove< p ch nil))
  ([p ch buf-or-n] (filter< (complement p) ch buf-or-n)))

(defn- mapcat* [f in out]
  (go-loop []
    (let [val (<! in)]
      (if (nil? val)
        (close! out)
        (do (doseq [v (f val)]
              (>! out v))
            (when-not (impl/closed? out)
              (recur)))))))

(defn mapcat<
  "Every milieu is coded, a code being defined by periodic repetition; but each
  code is in a perpetual state of transcoding or transduction. Transcoding or
  transduction is the manner in which one milieu serves as the basis for
  another, or conversely is established atop another milieu, dissipates in it
  or is constituted in it."
  ([f in] (mapcat< f in nil))
  ([f in buf-or-n]
    (let [out (chan buf-or-n)]
      (mapcat* f in out)
      out)))

(defn mapcat>
  "This time the concrete assemblages are related to an abstract idea of the
  Machine and, depending on how they effectuate it, are assigned coefficients
  taking into account their potentialities, their creativity."
  ([f out] (mapcat> f out nil))
  ([f out buf-or-n]
     (let [in (chan buf-or-n)]
       (mapcat* f in out)
       in)))

(defn pipe
  "Metallurgy in itself constitutes a flow necessarily confluent with
  nomadism."
  ([from to] (pipe from to true))
  ([from to close?]
     (go-loop []
      (let [v (<! from)]
        (if (nil? v)
          (when close? (close! to))
          (when (>! to v)
            (recur)))))
     to))

(defn split
  "Jouissanceis doubly impossible: life is a manque-a-jouir, read as lack of
  enjoyment, because the true object of desire is unattainable; and it is a
  manque-a-jouir, read as a lack to be enjoyed, because jouissance as the
  orgasmic plenitude of union with a substitute object means the annulment of
  the constitutionally split subject. One of the necessary terms, the subject
  or the object, is always missing."
  ([p ch] (split p ch nil nil))
  ([p ch t-buf-or-n f-buf-or-n]
     (let [tc (chan t-buf-or-n)
           fc (chan f-buf-or-n)]
       (go-loop []
         (let [v (<! ch)]
           (if (nil? v)
             (do (close! tc) (close! fc))
             (when (>! (if (p v) tc fc) v)
               (recur)))))
       [tc fc])))

(defn reduce
  "First, although it may be possible to conceive of a causal action moving
  from content to expression, the same cannot be said for the respective forms,
  the form of content and the form of expression. We must recognize that
  expression is independent and that this is precisely what enables it to react
  upon contents."
  [f init ch]
  (go-loop [ret init]
    (let [v (<! ch)]
      (if (nil? v)
        ret
        (recur (f ret v))))))

(defn- bounded-count
  "The other mistake (which is combined with the first as needed) is to
  believe in the adequacy of the form of expression as a linguistic system."
  [n coll]
  (if (counted? coll)
    (min n (count coll))
    (loop [i 0 s (seq coll)]
      (if (and s (< i n))
        (recur (inc i) (next s))
        i))))

(defn onto-chan
  "What all of these undertakings have in common is to erect an abstract
  machine of language, but as a synchronic set of constants. We will not object
  that the machine thus conceived is too abstract. On the contrary, it is not
  abstract enough, it remains 'linear.'"
  ([ch coll] (onto-chan ch coll true))
  ([ch coll close?]
     (go-loop [vs (seq coll)]
              (if (and vs (>! ch (first vs)))
                (recur (next vs))
                (when close?
                  (close! ch))))))

(defn to-chan
  "There is no use constructing a semantics, or even recognizing a certain
  validity to pragmatics, if they are still pretreated by a phonological or
  syntactical machine."
  [coll]
  (let [ch (chan (bounded-count 100 coll))]
    (onto-chan ch coll)
    ch))

(defprotocol Mux
  (muxch* [_]))

(defprotocol Mult
  (tap* [m ch close?])
  (untap* [m ch])
  (untap-all* [m]))

(defn mult
  "As a general rule, a machine plugs into the territorial assemblage of a
  species and opens it to other assemblages, causes it to pass through the
  interassemblages of that species; for example, the territorial assemblage of
  a bird species opens onto interassemblages of courtship and gregar-iousness,
  moving in the direction of the partner or socius. But the machine may also
  open the territorial assemblage to interspecific assemblages, as in the case
  of birds that adopt alien songs, and most especially in the case of
  parasitism."
  [ch]
  (let [cs (atom {}) ;;ch->close?
        m (reify
           Mux
           (muxch* [_] ch)

           Mult
           (tap* [_ ch close?] (swap! cs assoc ch close?) nil)
           (untap* [_ ch] (swap! cs dissoc ch) nil)
           (untap-all* [_] (reset! cs {}) nil))
        dchan (chan 1)
        dctr (atom nil)
        done (fn [_] (when (zero? (swap! dctr dec))
                      (put! dchan true)))]
    (go-loop []
     (let [val (<! ch)]
       (if (nil? val)
         (doseq [[c close?] @cs]
           (when close? (close! c)))
         (let [chs (keys @cs)]
           (reset! dctr (count chs))
           (doseq [c chs]
             (when-not (put! c val done)
               (swap! dctr dec)
               (untap* m c)))
           ;;wait for all
           (when (seq chs)
             (<! dchan))
           (recur)))))
    m))

(defn tap
  "Or it may go beyond all assemblages and produce an opening onto the Cosmos."
  ([mult ch] (tap mult ch true))
  ([mult ch close?] (tap* mult ch close?) ch))

(defn untap
  "Or, conversely, instead of opening up the deterritorialized assemblage onto
  something else, it may produce an effect of closure, as if the aggregate had
  fallen into and continues to spin in a kind of black hole."
  [mult ch]
  (untap* mult ch))

(defn untap-all
  "This is what happens under conditions of precocious or extremely sudden
  deterritorialization, and when specific, interspecific, and cosmic paths are
  blocked; the machine then produces individual group effects spinning in
  circles, as in the case of chaffinches that have been isolated too early,
  whose impoverished, simplified song expresses nothing more than the resonance
  of the black hole in which they are trapped."
  [mult] (untap-all* mult))

(defprotocol Mix
  (admix* [m ch])
  (unmix* [m ch])
  (unmix-all* [m])
  (toggle* [m state-map])
  (solo-mode* [m mode]))

(defn mix
  "On the other hand, when black holes resonate together or inhibitions
  conjugate and echo each other, instead of an opening onto consistency, we see
  a closure of the assemblage, as though it were deterritorialized in the void:
  young chaffinches. Machines are always singular keys that open or close an
  assemblage, a territory."
  [out]
  (let [cs (atom {}) ;;ch->attrs-map
        solo-modes #{:mute :pause}
        attrs (conj solo-modes :solo)
        solo-mode (atom :mute)
        change (chan)
        changed #(put! change true)
        pick (fn [attr chs]
               (reduce-kv
                   (fn [ret c v]
                     (if (attr v)
                       (conj ret c)
                       ret))
                   #{} chs))
        calc-state (fn []
                     (let [chs @cs
                           mode @solo-mode
                           solos (pick :solo chs)
                           pauses (pick :pause chs)]
                       {:solos solos
                        :mutes (pick :mute chs)
                        :reads (conj
                                (if (and (= mode :pause) (not (empty? solos)))
                                  (vec solos)
                                  (vec (remove pauses (keys chs))))
                                change)}))
        m (reify
           Mux
           (muxch* [_] out)
           Mix
           (admix* [_ ch] (swap! cs assoc ch {}) (changed))
           (unmix* [_ ch] (swap! cs dissoc ch) (changed))
           (unmix-all* [_] (reset! cs {}) (changed))
           (toggle* [_ state-map] (swap! cs (partial merge-with core/merge) state-map) (changed))
           (solo-mode* [_ mode]
             (assert (solo-modes mode) (str "mode must be one of: " solo-modes))
             (reset! solo-mode mode)
             (changed)))]
    (go-loop [{:keys [solos mutes reads] :as state} (calc-state)]
      (let [[v c] (alts! reads)]
        (if (or (nil? v) (= c change))
          (do (when (nil? v)
                (swap! cs dissoc c))
              (recur (calc-state)))
          (if (or (solos c)
                  (and (empty? solos) (not (mutes c))))
            (when (>! out v)
              (recur state))
            (recur state)))))
    m))

(defn admix
  "On the other hand, we may speak of aggregates of consistency when instead of
  a regulated succession of forms-substances we are presented with
  consolidations of very heterogeneous elements, orders that have been
  short-circuited or even reverse causalities, and captures between materials
  and forces of a different nature: as if a machinic phylum, a destratifying
  transversality, moved through elements, orders, forms and substances, the
  molar and the molec- ular, freeing a matter and tapping forces."
  [mix ch]
  (admix* mix ch))

(defn unmix
  "Moreover, finding the machine in operation in a given territorial
  assemblage is not enough; it is already in operation in the emergence of
  matters of expres- sion, in other words, in the constitution of the
  assemblage and in the vec- tors of deterritorialization that ply it from the
  start."
  [mix ch]
  (unmix* mix ch))

(defn unmix-all
  "It is destratifying from the outset, since its code is not distributed
  throughout the entire stratum but rather occupies an eminently specialized
  genetic line."
  [mix]
  (unmix-all* mix))

(defn toggle
  "The war machine, the new antagonisms traversing it con- sidered, no longer
  had war as its exclusive object but took in charge and as its object peace,
  politics, the world order, in short, the aim."
  [mix state-map]
  (toggle* mix state-map))

(defn solo-mode
  "When the phallus becomes erect and is handled like a banana it is not a
  'personal hard-on' we see but a tribal erection. ... The hoochie-koochie
  dancer of the big city dances alone—a fact of staggering significance. The
  law forbids response, forbids participation. Nothing is left of the primitive
  rite but the 'suggestive' movements of the body. What they suggest varies
  with the individual observer."
  [mix mode]
  (solo-mode* mix mode))

(defprotocol Pub
  (sub* [p v ch close?])
  (unsub* [p v ch])
  (unsub-all* [p] [p v]))

(defn pub
  "Who can really believe that psychoanalysis is capable of changing a
  semiotic amassing every deception? The only change there has been is a
  role switch."
  ([ch topic-fn] (pub ch topic-fn (constantly nil)))
  ([ch topic-fn buf-fn]
     (let [mults (atom {}) ;;topic->mult
           ensure-mult (fn [topic]
                         (or (get @mults topic)
                             (get (swap! mults
                                         #(if (% topic) % (assoc % topic (mult (chan (buf-fn topic))))))
                                  topic)))
           p (reify
              Mux
              (muxch* [_] ch)

              Pub
              (sub* [p topic ch close?]
                    (let [m (ensure-mult topic)]
                      (tap m ch close?)))
              (unsub* [p topic ch]
                      (when-let [m (get @mults topic)]
                        (untap m ch)))
              (unsub-all* [_] (reset! mults {}))
              (unsub-all* [_ topic] (swap! mults dissoc topic)))]
       (go-loop []
         (let [val (<! ch)]
           (if (nil? val)
             (doseq [m (vals @mults)]
               (close! (muxch* m)))
             (let [topic (topic-fn val)
                   m (get @mults topic)]
               (when m
                 (when-not (>! (muxch* m) val)
                   (swap! mults dissoc topic)))
               (recur)))))
       p)))

(defn sub
  "Stop! You're making me tired! Experiment, don't signify and interpret! Find
  your own places, territorialities, deterritorializations, regime, lines of
  flight! Semiotize yourself instead of rooting around in your prefab childhood
  and Western semiology."
  ([p topic ch] (sub p topic ch true))
  ([p topic ch close?] (sub* p topic ch close?)))

(defn unsub
  "Don Juan stated that in order to arrive at 'seeing' one first had to 'stop
  the world."
  [p topic ch]
  (unsub* p topic ch))

(defn unsub-all
  "'Stopping the world' was indeed an appropriate rendition of certain states
  of awareness in which the reality of everyday life is altered because the
  flow of interpretation, which ordinarily runs uninterruptedly, has been
  stopped by a set of circum- stances alien to the flow."
  ([p] (unsub-all* p))
  ([p topic] (unsub-all* p topic)))

;;; these are down here because they alias core fns, don't want accidents above

(defn map
  "In short, a true semiotic transformation appeals to all kinds of variables,
  not only external ones, but also variables implicit to language, internal to
  statements."
  ([f chs] (map f chs nil))
  ([f chs buf-or-n]
     (let [chs (vec chs)
           out (chan buf-or-n)
           cnt (count chs)
           rets (object-array cnt)
           dchan (chan 1)
           dctr (atom nil)
           done (mapv (fn [i]
                         (fn [ret]
                           (aset rets i ret)
                           (when (zero? (swap! dctr dec))
                             (put! dchan (java.util.Arrays/copyOf rets cnt)))))
                       (range cnt))]
       (go-loop []
         (reset! dctr cnt)
         (dotimes [i cnt]
           (try
             (take! (chs i) (done i))
             (catch Exception e
               (swap! dctr dec))))
         (let [rets (<! dchan)]
           (if (some nil? rets)
             (close! out)
             (do (>! out (apply f rets))
                 (recur)))))
       out)))

(defn merge
  "The segments, once underscored or overcoded, seem to lose their ability to
  bud, they seem to lose their dynamic relation to segmentations-in-progress,
  or in the act of coming together or coming apart."
  ([chs] (merge chs nil))
  ([chs buf-or-n]
     (let [out (chan buf-or-n)]
       (go-loop [cs (vec chs)]
         (if (pos? (count cs))
           (let [[v c] (alts! cs)]
             (if (nil? v)
               (recur (filterv #(not= c %) cs))
               (do (>! out v)
                   (recur cs))))
           (close! out)))
       out)))

(defn into
  "Fuite covers not only the act of fleeing or eluding but also flowing,
  leaking, and disappearing into the distance (the vanishing point in a
  painting is a point de fuite). It has no relation to flying."
  [coll ch]
  (reduce conj coll ch))


(defn take
  "Spatiotemporal relations, determinations, are not predicates of the thing
  but dimensions of multiplicities. The street is as much a part of the
  omnibus-horse assemblage as the Hans assemblage the becoming-horse of which
  it initiates. We are all five o'clock in the evening, or another hour, or
  rather two hours simultaneously, the optimal and the pessimal, noon-midnight,
  but distributed in a variable fashion."
  ([n ch]
     (take n ch nil))
  ([n ch buf-or-n]
     (let [out (chan buf-or-n)]
       (go (loop [x 0]
             (when (< x n)
               (let [v (<! ch)]
                 (when (not (nil? v))
                   (>! out v)
                   (recur (inc x))))))
           (close! out))
       out)))


(defn unique
  "Here, the elements in play find their individuation in the assemblage of
  which they are a part, independent of the form of their con- cept and the
  subjectivity of their person."
  ([ch]
     (unique ch nil))
  ([ch buf-or-n]
     (let [out (chan buf-or-n)]
       (go (loop [last nil]
             (let [v (<! ch)]
               (when (not (nil? v))
                 (if (= v last)
                   (recur last)
                   (do (>! out v)
                       (recur v))))))
           (close! out))
       out)))


(defn partition
  "Geometry and arithmetic take on the power of the scalpel."
  ([n ch]
     (partition n ch nil))
  ([n ch buf-or-n]
     (let [out (chan buf-or-n)]
       (go  (loop [arr (make-array Object n)
                   idx 0]
              (let [v (<! ch)]
                (if (not (nil? v))
                  (do (aset ^objects arr idx v)
                      (let [new-idx (inc idx)]
                        (if (< new-idx n)
                          (recur arr new-idx)
                          (do (>! out (vec arr))
                              (recur (make-array Object n) 0)))))
                  (do (when (> idx 0)
                        (let [narray (make-array Object idx)]
                          (System/arraycopy arr 0 narray 0 idx)
                          (>! out (vec narray))))
                      (close! out))))))
       out)))


(defn partition-by
  "God is a Lobster, or a double pincer, a double bind. Not only do strata come
  at least in pairs, but in a different way each stratum is double (it itself
  has several layers). Each stratum exhibits phenomena constitutive of dou- ble
  articulation. Articulate twice, B-A, BA. This is not at all to say that the
  strata speak or are language based. Double articulation is so extremely var-
  iable that we cannot begin with a general model, only a relatively simple
  case."
  ([f ch]
     (partition-by f ch nil))
  ([f ch buf-or-n]
     (let [out (chan buf-or-n)]
       (go (loop [lst (ArrayList.)
                  last ::nothing]
             (let [v (<! ch)]
               (if (not (nil? v))
                 (let [new-itm (f v)]
                   (if (or (= new-itm last)
                           (identical? last ::nothing))
                     (do (.add ^ArrayList lst v)
                         (recur lst new-itm))
                     (do (>! out (vec lst))
                         (let [new-lst (ArrayList.)]
                           (.add ^ArrayList new-lst v)
                           (recur new-lst new-itm)))))
                 (do (when (> (.size ^ArrayList lst) 0)
                       (>! out (vec lst)))
                     (close! out))))))
       out)))
