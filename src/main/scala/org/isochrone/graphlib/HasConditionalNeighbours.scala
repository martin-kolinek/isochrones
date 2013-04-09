package org.isochrone.graphlib

trait HasConditionalNeighbours[T, Cond] {
    def condNeighbours(t:T, cond:Cond):Traversable[(T, Double)]
}

class WithConditionalNeighbours[T, Cond](t:T)(implicit e:HasConditionalNeighbours[T, Cond]) {
    def condNeighbours(cond:Cond) = e.condNeighbours(t, cond)
}
