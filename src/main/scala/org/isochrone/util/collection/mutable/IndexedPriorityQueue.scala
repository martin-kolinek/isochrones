package org.isochrone.util.collection.mutable

import scala.collection.mutable.Set
import scala.collection.mutable.HashSet
import java.util.TreeMap
import scala.collection.mutable.Map
import scala.collection.immutable.SortedMap
import scala.collection.mutable.TreeSet
import scala.collection.mutable.HashMap

class IndexedPriorityQueue[ItemType, PriorityType: Ordering] private (
        val priorities: TreeMap[PriorityType, SortedMap[Long, ItemType]],
        val inverse: Map[ItemType, PriorityType],
        val insertIndexMap: Map[ItemType, Long]) extends Iterable[(ItemType, PriorityType)] {
    val imp = implicitly[Ordering[PriorityType]]
    import imp._
    var insertIndex = 0l
    def +=(item: (ItemType, PriorityType)) {
        val haveGreater = inverse.get(item._1).map(_ > item._2)
        if (haveGreater.getOrElse(false))
            this -= item._1
        if (haveGreater.getOrElse(true)) {
            val st = priorities.get(item._2)
            if (st == null) {
                priorities.put(item._2, SortedMap(insertIndex -> item._1))
            } else {
                priorities.put(item._2, st + (insertIndex -> item._1))
            }
            insertIndexMap(item._1) = insertIndex
            insertIndex += 1
            inverse += item
        }
    }

    def -=(item: ItemType) {
        val opt = for {
            prio <- inverse.get(item)
            st <- Option(priorities.get(prio))
        } yield (prio, st)
        if (opt.isEmpty)
            return
        val (priority, map) = opt.get
        val newMap = map - insertIndexMap(item)
        if (newMap.isEmpty)
            priorities.remove(priority)
        else
            priorities.put(priority, newMap)
        inverse -= item
    }

    def minimum = priorities.firstEntry.getValue.head._2 -> priorities.firstEntry.getKey

    def maximum = priorities.lastEntry.getValue.head._2 -> priorities.lastEntry.getKey

    def empty = priorities.isEmpty

    def ++=(items: Traversable[(ItemType, PriorityType)]) {
        for (i <- items)
            this += i
    }

    def --=(items: Traversable[ItemType]) {
        for (i <- items)
            this -= i
    }

    def iterator = inverse.iterator

    override def toString = s"IndexedPriorityQueue($priorities)"
}

object IndexedPriorityQueue {
    def apply[ItemType, PriorityType: Ordering](items: (ItemType, PriorityType)*) = {
        val ret = new IndexedPriorityQueue[ItemType, PriorityType](new TreeMap[PriorityType, SortedMap[Long, ItemType]](implicitly[Ordering[PriorityType]]), new HashMap, new HashMap)
        for (i <- items)
            ret += i
        ret
    }
}
