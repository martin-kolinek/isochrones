package org.isochrone.connect

import com.typesafe.scalalogging.slf4j.Logging
import scala.annotation.tailrec

/*class DisjointSets[T] private (mp: Map[T, Set[T]]) extends Logging {
    def this(ts: Seq[T]) = this(ts.map(x => x -> Set(x)).toMap)
    def find(t: T): Set[T] = mp(t)
    def union(t1: T, t2: T): DisjointSets[T] = {
        val union = find(t1) union find(t2)
        val newmp = (mp /: union)((m, t) => m.updated(t, union))
        new DisjointSets(newmp)
    }

    def allSets = mp.values.toSet
}*/

class DisjointSetStructure[T](ds: Traversable[T]) {

    class Node(val elem: T) {
        var parent = this
        var rank = 0
    }

    val trees = ds.map(x => x -> new Node(x)).toMap

    def find(x: Node): Node = {
        if (x.parent == x)
            x
        else {
            val ret = find(x.parent)
            x.parent = ret
            ret
        }
    }

    def union(a: T, b: T): Unit = {
        val at = find(trees(a))
        val bt = find(trees(b))

        if (at == bt) {
            return
        }

        if (at.rank < bt.rank) {
            at.parent = bt
        } else if (at.rank > bt.rank) {
            bt.parent = at
        } else {
            at.parent = bt
            at.rank += 1
        }
    }

    def allSets = {
        trees.values.groupBy(find).values.map(_.map(_.elem).toSet)
    }
} 