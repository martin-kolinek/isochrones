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
import org.isochrone.dbgraph.DBGraphConfigParserComponent
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.dijkstra.DijkstraAlgorithmProviderComponent
import scala.collection.mutable.HashSet
import org.isochrone.dbgraph.ReverseDatabaseGraph

trait HHStepComponent extends DBGraphConfigParserComponent with GraphComponentBase with HigherDescendLimitFinderComponent with ShortcutReverseLimitFinderComponent with HigherNodeRemoverComponent with ReverseNeighbourhoodFinderComponent {
    self: HigherLevelRoadNetTableComponent with RegularPartitionComponent with HigherHHTableComponent with HHTableComponent with RoadNetTableComponent with DatabaseProvider with ArgumentParser with FirstPhaseComponent with SecondPhaseComponent with NeighbourhoodSizeFinderComponent with LineContractionComponent with DijkstraAlgorithmProviderComponent =>

    override type NodeType = Long

    object HHStep extends Logging {
        def createHigherLevel() {
            logger.info("Creating higher level")
            database.withTransaction { implicit ses: Session =>
                val hhgraph = new HHDatabaseGraph(hhTables, roadNetTables, None, 200, ses)
                val fs = firstPhase(hhgraph, hhgraph)
                val ss = secondPhase(hhgraph, hhgraph)
                val regcount = regularPartition.regions.size
                higherRoadNetTables.roadNodes.insert(roadNetTables.roadNodes)
                regularPartition.regions.zipWithIndex.foreach {
                    case (reg, regidx) => {
                        logger.info(s"Processing region $regidx/$regcount")
                        val alreadyAdded = new HashSet[(NodeType, NodeType)]
                        roadNetTables.roadNodes.filter(_.geom @&& reg.dbBBox).sortBy(_.id).map(_.id).zipWithIndex.foreach {
                            case (rn, idx) => {
                                logger.debug(s"Processing node $rn (idx = $idx)")
                                val tree = fs.nodeTree(rn)
                                val hhedges = ss.extractHighwayEdges(tree)
                                logger.debug(s"Found ${hhedges.size} highway edges")
                                hhedges.foreach {
                                    case edg@(s, e) if !alreadyAdded.contains(edg) => {
                                        val insQ = for {
                                            r <- roadNetTables.roadNet if r.start === s && r.end === e
                                            if !higherRoadNetTables.roadNet.filter(h => h.start === r.start && h.end === r.end).exists
                                        } yield r
                                        higherRoadNetTables.roadNet.insert(insQ)
                                        alreadyAdded += edg
                                    }
                                    case _ => {}
                                }
                            }
                        }
                    }
                }

                val reversed = for {
                    e <- roadNetTables.roadNet
                    if higherRoadNetTables.roadNet.filter(rev => rev.start === e.end && rev.end === e.start).exists &&
                        !higherRoadNetTables.roadNet.filter(e2 => e2.start === e.start && e2.end === e.end).exists
                } yield e
                logger.info("Inserting reverse edges")
                higherRoadNetTables.roadNet.insert(reversed)
            }
        }

        def contractLines() {
            logger.info("Contracting higher level")
            database.withTransaction { implicit s: Session =>

                for ((reg, i) <- regularPartition.regions.zipWithIndex) {
                    val lcontractor = lineContractor(new DatabaseGraph(higherRoadNetTables, dbGraphConfLens.get(parsedConfig).effectiveNodeCacheSize, s), higherRoadNetTables, higherHHTables.shortcutEdges, higherHHTables.reverseShortcutEdges)
                    logger.info(s"Processing region $i/${regularPartition.regionCount}")
                    lcontractor.contractLines(reg.dbBBox)
                }
            }
        }

        def contractTrees() {
            logger.info("Contracting trees")
            database.withTransaction { implicit s: Session =>
                TreeContraction.contractTrees(higherRoadNetTables, higherHHTables.shortcutEdges, higherHHTables.reverseShortcutEdges, s)
            }
        }

        def findNeighbourhoodSizes() {
            logger.info("Finding neighbourhood sizes")
            database.withTransaction { implicit s: Session =>
                for (
                    (g, out) <- Seq(
                        new DatabaseGraph(roadNetTables, dbGraphConfLens.get(parsedConfig).effectiveNodeCacheSize, s) -> hhTables.neighbourhoods,
                        new ReverseDatabaseGraph(roadNetTables, dbGraphConfLens.get(parsedConfig).effectiveNodeCacheSize, s) -> hhTables.reverseNeighbourhoods)
                ) {
                    val finder = neighSizeFinder(g)
                    roadNetTables.roadNodes.sortBy(_.id).map(_.id).foreach { n =>
                        logger.info(s"Processing node $n")
                        finder.saveNeighbourhoodSize(n, out)
                    }
                }
            }

            ReverseNeighbourhoodFinder.findReverseNeighbourhoods()
        }

        def contractAll() {
            contractTrees()
            contractLines()
        }

        def findHigherDescendLimits() {
            HigherDescendLimitFinder.findDescendLimits()
        }

        def findShortcutReverseLimits() {
            ShorctutReverseLimitFinder.findShortcutReverseLimits(higherHHTables.shortcutEdges, higherHHTables.descendLimit, higherHHTables.shortcutReverseLimit)
        }

        def removeUnneededHigherNodes() {
            HigherNodeRemover.removeHigherNodes(higherRoadNetTables, higherHHTables)
        }

        def makeStep() {
            findNeighbourhoodSizes()
            createHigherLevel()
            contractAll()
            removeUnneededHigherNodes()
            findHigherDescendLimits()
            findShortcutReverseLimits()
        }
    }
}

trait DefaultHHStepComponent
        extends ConfigRegularPartitionComponent
        with FirstPhaseComponentImpl
        with SecondPhaseComponent
        with LineContractionComponent
        with ConfigNeighbourhoodCountComponent
        with DijkstraAlgorithmProviderComponent
        with NeighbourhoodSizeFinderComponent
        with ConfigHigherHHTableComponent
        with HHStepComponent
        with FirstPhaseParametersFromArg {
    self: HigherLevelRoadNetTableComponent with RoadNetTableComponent with ArgumentParser with DatabaseProvider with HHTableComponent =>

    override type NodeType = Long
}