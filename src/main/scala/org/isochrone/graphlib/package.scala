package org.isochrone

import scala.language.implicitConversions

package object graphlib {
	implicit def toWithNeighbours[T:HasNeighbours](t:T) = new WithNeighbours(t)
}
