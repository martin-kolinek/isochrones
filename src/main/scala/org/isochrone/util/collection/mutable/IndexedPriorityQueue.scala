package org.isochrone.util.collection.mutable

import scala.collection.mutable.Set
import scala.collection.mutable.HashSet
import java.util.TreeMap
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap

class IndexedPriorityQueue[ItemType, PriorityType] private (
		val priorities:TreeMap[PriorityType, Set[ItemType]],
		val inverse:Map[ItemType, PriorityType]) extends Iterable[(ItemType, PriorityType)] {
	
	def +=(item:(ItemType, PriorityType)) {
        val st = priorities.get(item._2)
        if(st == null) {
            priorities.put(item._2, Set(item._1))
        }
        else {
            st += item._1
        }
        inverse+=item
	}
	
    private def nullToOpt[T](t:T) = if(t==null) None else Some(t)

	def -=(item:ItemType) {
		val opt = for {
			prio <- inverse.get(item)
			st <- nullToOpt(priorities.get(prio))
        } yield (prio, st)
        if(opt.isEmpty)
            return
        val (priority, set) = opt.get
        set -= item
        if(set.isEmpty)
            priorities.remove(priority)
        inverse -= item
	}
	
	def minimum = priorities.firstEntry.getValue.head -> priorities.firstEntry.getKey
	
	def maximum = priorities.lastEntry.getValue.head -> priorities.lastEntry.getKey
	
	def empty = priorities.isEmpty
	
	def ++=(items:Traversable[(ItemType, PriorityType)]) {
		for(i<-items)
            this += i
	}
	
	def --=(items:Traversable[ItemType]) {
        for(i<-items)
            this -= i
	}
	
	def iterator = inverse.iterator
	
	override def toString = s"IndexedPriorityQueue($priorities)"
}

object IndexedPriorityQueue {
	def apply[ItemType, PriorityType:Ordering](items:(ItemType, PriorityType)*) = {
        val priorities = new TreeMap[PriorityType, Set[ItemType]](implicitly[Ordering[PriorityType]])
		for((k,v) <- items.groupBy(_._2).map(x=> x._1 -> HashSet(x._2.map(_._1):_*)))
            priorities.put(k,v);
		new IndexedPriorityQueue[ItemType, PriorityType](priorities, HashMap(items:_*))
	}
}
