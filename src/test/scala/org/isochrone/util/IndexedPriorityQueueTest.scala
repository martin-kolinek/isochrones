package org.isochrone.util

import org.scalatest.FunSuite
import org.isochrone.util.collection.mutable.IndexedPriorityQueue

class IndexedPriorityQueueTest extends FunSuite {
	test("mutable indexed priority queue can add items and get minimum and maximum") {
		val ipq = IndexedPriorityQueue[String, Int]()
		ipq += ("hello", 1)
		ipq += ("world", 2)
		assert(ipq.minimum=="hello"->1)
		assert(ipq.maximum=="world"->2)
		assert(ipq.toSet == Set("hello" -> 1, "world" -> 2))
	}
	
	test("replacing item in priority queue works") {
		val ipq = IndexedPriorityQueue[String, Int]()
		ipq += ("hello", 2)
		ipq += ("world", 3)
		ipq += ("hello", 4)
		assert(ipq.toSet == Set("hello" -> 2, "world" -> 3))
		ipq += ("hello", 1)
		assert(ipq.toSet == Set("hello" -> 1, "world" -> 3))
	}
}