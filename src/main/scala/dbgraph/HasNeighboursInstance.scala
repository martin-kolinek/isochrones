package dbgraph

import graphlib.HasNeighbours
import scala.slick.session.Session

class HasNeighboursInstance(g:DatabaseGraph) {
	implicit val nodeHasNeighbours = new HasNeighbours[Long] {
		def neighbours(n:Long) = g.getNeighbours(n)
	}
}
