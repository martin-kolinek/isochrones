package org.isochrone.executors

import org.isochrone.ActionExecutor
import org.isochrone.ActionComponent

trait HigherLevelCreator extends ActionExecutor {
	abstract override def actions = super.actions + ("higher" -> new ActionComponent {
	    val execute = () => println("higher")
	})
}