package org.isochrone.graphlib

trait HasNeighbours[T] {
	def neighbours(node:T) : Traversable[(T, Double)]
}

class WithNeighbours[T:HasNeighbours](t:T) {
	def neighbours = implicitly[HasNeighbours[T]].neighbours(t)
}

