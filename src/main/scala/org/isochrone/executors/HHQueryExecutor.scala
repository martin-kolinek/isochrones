package org.isochrone.executors

import org.isochrone.ActionExecutor
import org.isochrone.ActionComponent
import org.isochrone.Main
import org.isochrone.hh.HHIsochroneComputer
import org.isochrone.dijkstra.DijkstraAlgorithmProviderComponent
import org.isochrone.dbgraph.MultiLevelHHDatabaseGraphComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.db.SingleSessionProvider
import org.isochrone.db.MultiLevelRoadNetTableComponent
import org.isochrone.ArgumentParser
import org.isochrone.db.ConfigMultiLevelRoadNetTableComponent
import org.isochrone.hh.ConfigMultiLevelHHTableComponent
import org.isochrone.dijkstra.GenericDijkstraAlgorithmProvider
import org.isochrone.graphlib.GraphComponentBaseWithDefault
import scopt.Read

trait HHQueryExecutor extends ActionExecutor {
    self: Main.type =>
	abstract override def actions = super.actions + ("hhisochrone" --> 
	new ActionComponent
	with IsochroneExecutorCompoent
	with OptionsBase
	with ArgumentParser 
	with GenericDijkstraAlgorithmProvider
	with ConfigMultiLevelRoadNetTableComponent
	with ConfigMultiLevelHHTableComponent
	with MultiLevelHHDatabaseGraphComponent
	with HHIsochroneComputer
	with FromOptionDatabaseComponent
	with SingleSessionProvider
	with GraphComponentBaseWithDefault {
	    def readNodeType = implicitly[Read[NodeType]]
	    def noNode = 0l
	    override type NodeType = Long
	})
}