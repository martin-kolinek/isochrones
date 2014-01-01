package org.isochrone.util

trait ListVectorImplicit {
    implicit class ListVector[T](lst: List[T]) {
        def x = lst.head
        def y = lst.tail.head
    }
    def vector[T] = (x: T, y: T) => List(x, y)
}
