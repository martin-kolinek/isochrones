package org.isochrone.graphlib

trait HasComputableIsochrone[T] {
	def isochrone(start:Traversable[(T, Double)], max:Double, resultCallback:T=>Unit)
}

class WithIsochrone[T:HasComputableIsochrone](t:T) {
	val i = implicitly[HasComputableIsochrone[T]]
	/**
	 * Returns a collection of nodes which are part of isochrone
	 */
	def isochrone(limit:Double) = new Traversable[T] { 
		def foreach[X](f:T=>X) = i.isochrone(Seq((t, 0)), limit, f(_)) 
	}
}