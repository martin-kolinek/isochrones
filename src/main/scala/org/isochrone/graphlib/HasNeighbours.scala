package org.isochrone.graphlib

import scala.language.implicitConversions

trait HasNeighbours[T] {
	def neighbours(node:T) : Traversable[(T, Double)]
}

class WithNeighbours[T:HasNeighbours](t:T) {
	def neighbours = implicitly[HasNeighbours[T]].neighbours(t)
}

package object graphlibObj {
	implicit def toWithNeighbours[T:HasNeighbours](t:T) = new WithNeighbours(t)
}
