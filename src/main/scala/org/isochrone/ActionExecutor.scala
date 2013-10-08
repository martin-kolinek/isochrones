package org.isochrone

import scala.collection.mutable.HashMap

trait ActionExecutor {
    def actions: Map[String, ActionComponent] = Map()
}