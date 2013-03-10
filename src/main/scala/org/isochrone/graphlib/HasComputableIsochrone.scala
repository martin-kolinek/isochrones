package org.isochrone.graphlib

trait HasComputableIsochrone[T] {
	def isochrone(start:T, resultCallback:T=>Unit)
}

class WithIsochrone[T:HasComputableIsochrone](t:T) {
	val i = implicitly[HasComputableIsochrone[T]]
	/**
	 * Returns a lazy collection of nodes which are part of isochrone
	 */
	def isochrone = new Traversable[T] { def foreach[X](f:T=>X) = i.isochrone(t, f(_)) }.view 
}