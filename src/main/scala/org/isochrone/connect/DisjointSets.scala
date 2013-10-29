package org.isochrone.connect

import com.typesafe.scalalogging.slf4j.Logging

class DisjointSets[T] private (mp: Map[T, Set[T]]) extends Logging {
    def this(ts: Seq[T]) = this(ts.map(x => x -> Set(x)).toMap)
    def find(t: T): Set[T] = mp(t)
    def union(t1: T, t2: T): DisjointSets[T] = {
        val union = find(t1) union find(t2)
        val newmp = (mp /: union)((m, t) => m.updated(t, union))
        new DisjointSets(newmp)
    }

    def allSets = mp.values.toSet
}