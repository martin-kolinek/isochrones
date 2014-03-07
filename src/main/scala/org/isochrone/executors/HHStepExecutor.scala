package org.isochrone.executors

import org.isochrone.Main
import org.isochrone.ActionExecutor
import org.isochrone.hh.DefaultHHStepComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.db.ConfigRoadNetTableComponent
import org.isochrone.hh.ConfigHHTableComponent
import org.isochrone.db.HigherConfigRoadNetTableComponent
import org.isochrone.ActionComponent

trait HHStepExecutor extends ActionExecutor {
    self: Main.type =>

    private trait Comp
        extends ActionComponent
        with OptionsBase
        with FromOptionDatabaseComponent
        with ConfigRoadNetTableComponent
        with ConfigHHTableComponent
        with HigherConfigRoadNetTableComponent
        with DefaultHHStepComponent

    abstract override def actions = super.actions + ("hhstep" -->
        new Comp {
            val execute = () => HHStep.makeStep()
        }) + ("hhneighsize" --> new Comp {
            val execute = () => HHStep.findNeighbourhoodSizes()
        }) + ("hhextract" --> new Comp {
            val execute = () => HHStep.createHigherLevel()
        }) + ("hhcontracttrees" --> new Comp {
            val execute = () => HHStep.contractTrees()
        }) + ("hhcontractlines" --> new Comp {
            val execute = () => HHStep.contractLines()
        }) + ("hhcontract" --> new Comp {
            val execute = () => HHStep.contractAll()
        }) + ("hhdescendlim" --> new Comp {
            val execute = () => HHStep.findHigherDescendLimits()
        }) + ("hhshortcutrevlim" --> new Comp {
            val execute = () => HHStep.findShortcutReverseLimits()
        }) + ("hhfiltnodes" --> new Comp {
            val execute = () => HHStep.removeUnneededHigherNodes()
        })

}