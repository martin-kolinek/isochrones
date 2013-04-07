package org.isochrone.util.collection.immutable

import scala.collection.immutable.Set
import scala.collection.immutable.SortedMap

class IndexedPriorityQueue[ItemType, PriorityType] private (
		val priorities:SortedMap[PriorityType, Set[ItemType]],
		val inverse:Map[ItemType, PriorityType]) extends Iterable[ItemType] {
	
	def +(item:(ItemType, PriorityType)) = {
		val st = priorities.get(item._2).map(_+item._1).getOrElse(Set(item._1))
		val newPrio = priorities + (item._2 -> st)
		val newInv = inverse + item
		new IndexedPriorityQueue(newPrio, newInv)
	}
	
	def -(item:ItemType) = {
		val ret = for {
			prio <- inverse.get(item)
			st <- priorities.get(prio)
			newSt = st - item
			newPrio = if(newSt.isEmpty) priorities - prio else priorities.updated(prio, newSt)
			newInv = inverse - item
		} yield new IndexedPriorityQueue(newPrio, newInv)
		ret.getOrElse(this)
	}
	
	def minimum = priorities.head._2.head
	
	def maximum = priorities.last._2.head
	
	def +++(items:Traversable[(ItemType, PriorityType)]) = {
		(this /: items)(_+_)
	}
	
	def --(items:Traversable[ItemType]) = {
		(this /: items)(_-_)
	}
	
	def iterator = inverse.keysIterator
	
	override def toString = s"IndexedPriorityQueue($priorities)"
}

object IndexedPriorityQueue {
	def apply[ItemType, PriorityType:Ordering](items:(ItemType, PriorityType)*) = {
		val priorities = SortedMap(items.groupBy(_._2).map(x=> x._1 -> x._2.map(_._1).toSet).toSeq:_*)
		new IndexedPriorityQueue[ItemType, PriorityType](priorities, items.toMap)
	}
}