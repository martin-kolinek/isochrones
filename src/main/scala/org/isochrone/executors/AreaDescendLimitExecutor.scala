package org.isochrone.executors

import org.isochrone.ActionExecutor
import org.isochrone.Main
import org.isochrone.hh.AreaDescendLimitFinderComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.db.ConfigRoadNetTableComponent
import org.isochrone.hh.ConfigHHTableComponent
import org.isochrone.ArgumentParser
import org.isochrone.ActionComponent

trait AreaDescendLimitExecutor extends ActionExecutor {
    self: Main.type =>

        abstract override def actions = super.actions + ("areadesclim" --> 
            new ActionComponent 
            with AreaDescendLimitFinderComponent
            with OptionsBase
            with ArgumentParser
            with FromOptionDatabaseComponent
            with ConfigRoadNetTableComponent
            with ConfigHHTableComponent {
            	val execute = (AreaDescendLimitFinder.execute _)
            })
}