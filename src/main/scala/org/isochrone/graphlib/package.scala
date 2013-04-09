package org.isochrone

import scala.language.implicitConversions

package object graphlib {
	implicit def toWithNeighbours[T:HasNeighbours](t:T) = new WithNeighbours(t)
	implicit def toWithComputableIsochrone[T:HasComputableIsochrone](t:T) = new WithIsochrone(t)
    implicit def toWithConditionalNeighbours[T, Cond](t:T)(implicit e:HasConditionalNeighbours[T, Cond]) = new WithConditionalNeighbours[T, Cond](t)
    implicit def hasNeighboursThenHasConditionalNeighbours[T:HasNeighbours, Cond] = new HasConditionalNeighbours[T, Cond] {
        def condNeighbours(t:T, cond:Cond) = t.neighbours
    }
}
