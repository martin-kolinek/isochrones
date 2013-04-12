package org.isochrone.simplegraph

import org.scalatest.FunSuite

class SimpleGraphTest extends FunSuite {
	test("SimpleGraph works") {
		val sg = new SimpleGraph(
				    (1, 2, 0.1),
		    		(2, 3, 0.2),
		    		(2, 4, 0.3),
		    		(5, 2, 0.4),
		    		(5, 3, 0.5),
		    		(4, 5, 0.6),
		    		(3, 5, 0.7))
		val neigh = sg.neighbours(2)
		assert(neigh.toSet == Set((3, 0.2),(4, 0.3)))
		val neigh2 = sg.neighbours(5)
		assert(neigh2.toSet == Set((2, 0.4),(3, 0.5)))
	}
	
	test("SimpleGraph returns empty list for nonexistent node") {
		val sg = new SimpleGraph(
				    (1, 2, 0.1),
		    		(2, 3, 0.2),
		    		(2, 4, 0.3),
		    		(5, 2, 0.4),
		    		(5, 3, 0.5),
		    		(4, 5, 0.6),
		    		(3, 5, 0.7))
		val neigh = sg.neighbours(10)
		assert(neigh.size==0)
	}
	
}
