package org.isochrone.util

import java.util.LinkedHashMap
import scala.collection.mutable.Map
import scala.collection.JavaConverters._

class LRUCache[K, V](removePredicate : (K, V, LRUCache[K, V]) => Boolean) extends Map[K, V] {
	val self = this
	val underlying = new LinkedHashMap[K, V](16, 0.75f, true) {
		override def removeEldestEntry(entry:java.util.Map.Entry[K, V]) = 
				removePredicate(entry.getKey(), entry.getValue(), self)
	}.asScala
		
	def updateUsage(k:K) {underlying.get(k)}
	
	def get(k:K) = underlying.get(k) 
	
	def iterator = underlying.iterator
	
	def -= (k:K) = { underlying -= k; this }
	
	def += (t:(K, V)) = { underlying += t; this }
}