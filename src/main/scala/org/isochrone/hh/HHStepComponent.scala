package org.isochrone.hh

import org.isochrone.db.HigherLevelRoadNetTableComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.graphlib.GraphComponent
import org.isochrone.dijkstra.DijkstraAlgorithmComponent
import org.isochrone.dbgraph.DatabaseGraph
import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.dbgraph.HHDatabaseGraph
import org.isochrone.db.ConfigRegularPartitionComponent
import org.isochrone.db.RegularPartitionComponent
import org.isochrone.ArgumentParser
import org.isochrone.dbgraph.NodeCacheSizeParserComponent
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.graphlib.GraphComponentBase

trait HHStepComponent extends NodeCacheSizeParserComponent with GraphComponentBase {
    self: HigherLevelRoadNetTableComponent with RegularPartitionComponent with HHTableComponent with RoadNetTableComponent with NeighbourhoodSizeComponent with DatabaseProvider with ArgumentParser with FirstPhaseComponent with SecondPhaseComponent =>

    type NodeType = Long

    object HHStep extends Logging {
        def createHigherLevel() {
            database.withTransaction { implicit ses: Session =>
                val hhgraph = new HHDatabaseGraph(hhTables, roadNetTables, 200, ses)
                val fs = firstPhase(hhgraph, hhgraph)
                val ss = secondPhase(hhgraph, hhgraph)
                higherRoadNetTables.roadNodes.insert(Query(roadNetTables.roadNodes))
                Query(roadNetTables.roadNodes).map(_.id).foreach { rn =>
                    val tree = fs.nodeTree(rn)
                    ss.extractHighwayEdges(tree).foreach {
                        case (s, e) => {
                            higherRoadNetTables.roadNet.insert(Query(roadNetTables.roadNet).filter(x => x.start === s && x.end === e))
                        }
                    }
                }

                val reversed = for {
                    e <- roadNetTables.roadNet
                    if Query(higherRoadNetTables.roadNet).filter(rev => rev.start === e.end && rev.end === e.start).exists &&
                        !Query(higherRoadNetTables.roadNet).filter(e2 => e2.start === e.start && e2.end === e.end).exists
                } yield e
                higherRoadNetTables.roadNet.insert(reversed)
            }
        }

        def contractHigherLevel() {
            database.withTransaction { implicit s: Session =>

                for ((reg, i) <- regularPartition.regions.zipWithIndex) {
                    object Contractor extends LineContractionComponent with GraphComponent with RoadNetTableComponent {
                        val roadNetTables = self.roadNetTables
                        val graph = new DatabaseGraph(roadNetTables, nodeCacheSizeLens.get(parsedConfig), s)
                        object Contractor extends LineContraction(hhTables.shortcutEdges)
                    }
                    logger.info(s"Processing region $i/${regularPartition.regionCount}")
                    Contractor.Contractor.contractLines(reg.dbBBox)

                }

            }
        }
    }

}