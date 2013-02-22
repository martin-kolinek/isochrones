package org.isochrone

import scala.language.implicitConversions

package object graphlib {
	implicit def toWithNeighbours[T:HasNeighbours](t:T) = new WithNeighbours(t)
    //implicit def toGraphOps[T](t:T)(implicit imp:IsGraph[T]) = new WithIsGraphOps(t)
    //implicit def toGraphRegOps[T](t:T)(implicit imp:IsGraphWithRegions[T]) = new WithIsGraphWithRegionsOps(t)
}
