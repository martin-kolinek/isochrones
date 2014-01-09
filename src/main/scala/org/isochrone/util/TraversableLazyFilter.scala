package org.isochrone.util

trait TraversableLazyFilter {
    implicit class TravLazyFilter[T](underlying: Traversable[T]) {
        def lazyFilter(cond: T => Boolean) = new Traversable[T] {
            def foreach[U](func: T => U) = {
                underlying.foreach { x =>
                    if (cond(x))
                        func(x)
                    else {}
                }
            }
        }
    }
}