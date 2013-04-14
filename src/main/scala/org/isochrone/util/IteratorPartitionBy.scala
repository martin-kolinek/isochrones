package org.isochrone.util

import scala.collection.Iterator
import scala.collection.mutable.Queue
import scala.collection.mutable.ListBuffer

trait IteratorPartitionBy {
	
	
	implicit class IteratorPartitionExtension[T](it:Iterator[T]) {
		def partitionBy[R](f:T=>R):Iterator[List[T]] = new Iterator[List[T]] {
			val self = it.buffered
			def hasNext = self.hasNext
			def next = {
				val result = new ListBuffer[T]
				val first = f(self.head)
				while(hasNext && f(self.head) == first) {
					result.append(self.next())
				}
				result.toList
			}
		}
	}
}