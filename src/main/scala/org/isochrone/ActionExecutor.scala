package org.isochrone

import scala.collection.mutable.HashMap

trait ActionExecutor {
    def actions: Map[String, () => ActionComponent] = Map()

    implicit class LazyValueStringOps(str: String) {
        def -->(value: => ActionComponent) = (str -> (() => value))
    }
}