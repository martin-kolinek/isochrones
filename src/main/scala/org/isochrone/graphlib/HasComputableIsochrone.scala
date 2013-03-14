package org.isochrone.graphlib

trait HasComputableIsochrone[T] {
	def isochrone(start:Traversable[(T, Double)], max:Double, resultCallback:(T, Double)=>Unit)
}

class WithIsochrone[T:HasComputableIsochrone](t:T) {
	val i = implicitly[HasComputableIsochrone[T]]
	/**
	 * Returns a collection of nodes which are part of isochrone
	 */
	def isochrone(limit:Double) = new Traversable[(T, Double)] { 
		def foreach[X](f:((T, Double))=>X) = i.isochrone(Seq((t, 0)), limit, (x, y) => f(x, y)) 
	}
}