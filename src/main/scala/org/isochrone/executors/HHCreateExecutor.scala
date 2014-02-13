package org.isochrone.executors

import org.isochrone.ActionExecutor
import org.isochrone.hh.HHTableCreatorComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.hh.ConfigHHTableComponent
import org.isochrone.Main
import org.isochrone.ActionComponent

trait HHCreateExecutor extends ActionExecutor {
    self: Main.type =>

    private trait Comp extends ActionComponent
        with OptionsBase
        with FromOptionDatabaseComponent
        with ConfigHHTableComponent
        with HHTableCreatorComponent

    abstract override def actions = super.actions +
        ("hhcreate" --> new Comp {
            val execute = () => HHTableCreator.createTables()
        }) +
        ("hhdrop" --> new Comp {
            val execute = () => HHTableCreator.dropTables()
        })
}