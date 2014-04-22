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
import com.typesafe.scalalogging.slf4j.Logging

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
	with GraphComponentBaseWithDefault
	with Logging {
	    def readNodeType = implicitly[Read[NodeType]]
	    def noNode = 0l
	    override type NodeType = Long
	    override def report() {
	        hhDbGraphs.zipWithIndex.foreach { case (graph, level) =>
	            logger.info(s"HHDatabaseGraph($level) retrievals: ${graph.retrievals}, total: ${graph.totalTimeRetrieving}")
	            logger.info(s"HHDatabaseGraph($level) report: ${graph.usageReport}")
	        }
	        shortcutGraphs.zipWithIndex.foreach { case (graph, level) =>
	            logger.info(s"ShortcutGraph($level) retrievals: ${graph.retrievals}, total: ${graph.totalTimeRetrieving}")
	            logger.info(s"ShortcutGraph($level) report: ${graph.usageReport}")
	        }
	        reverseShortcutGraph.zipWithIndex.foreach { case (graph, level) =>
	            logger.info(s"ReverseShortcutGraph($level) retrievals: ${graph.retrievals}, total: ${graph.totalTimeRetrieving}")
	            logger.info(s"ReverseShortcutGraph($level) report: ${graph.usageReport}")
	        }
	    }
	})
}