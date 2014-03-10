package org.isochrone.hh

import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.graphlib.GraphType
import org.isochrone.db.DatabaseProvider
import org.isochrone.dijkstra.DijkstraAlgorithmProviderComponent
import org.isochrone.db.RegularPartitionComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.dbgraph.HHDatabaseGraph
import org.isochrone.ArgumentParser
import org.isochrone.db.BufferOptionParserComponent
import org.isochrone.dbgraph.DBGraphConfigParserComponent
import org.isochrone.db.HigherLevelRoadNetTableComponent
import com.typesafe.scalalogging.slf4j.Logging

trait NonDbHigherDescendLimitFinderComponent {
    self: GraphComponentBase with DijkstraAlgorithmProviderComponent =>
    trait NonDbHigherDescendLimitFinder extends Logging {
        def getDescendLimits(grp: GraphType[NodeType], neighSize: NeighbourhoodSizes[NodeType], descLim: DescendLimitProvider[NodeType], initial: Map[NodeType, Double], nodes: TraversableOnce[NodeType]): Map[NodeType, Double] = {
            def updateDescendLimit(mp: Map[NodeType, Double], start: NodeType): Map[NodeType, Double] = {
                logger.debug(s"Working on node $start")
                val ns = neighSize.neighbourhoodSize(start)
                val nodes = dijkstraForGraph(grp).helper.compute(start).view.takeWhile(_._2 <= ns)
                val startDescLimit = descLim.descendLimit(start)
                (mp /: nodes) {
                    case (map, (node, fromStart)) => {
                        if (!map.contains(node))
                            throw new Exception(s"Tried to modify not loaded node descend limit (node = $node)")
                        val current = map(node)
                        val newComp = fromStart + startDescLimit
                        logger.debug(s"Node $node, current = $current, newComp = $newComp")
                        map + (node -> math.max(current, newComp))
                    }
                }
            }
            (initial /: nodes)(updateDescendLimit)
        }
    }
}

trait HigherDescendLimitFinderComponent extends NonDbHigherDescendLimitFinderComponent with BufferOptionParserComponent with DBGraphConfigParserComponent with GraphComponentBase {
    self: DatabaseProvider with DijkstraAlgorithmProviderComponent with RegularPartitionComponent with RoadNetTableComponent with HHTableComponent with HigherHHTableComponent with HigherLevelRoadNetTableComponent with ArgumentParser =>
    type NodeType = Long
    object HigherDescendLimitFinder extends NonDbHigherDescendLimitFinder {

        def findDescendLimits() {
            val bufferSize = bufferSizeLens.get(parsedConfig)
            logger.debug(s"Buffer Size: $bufferSize")
            val totalRegs = regularPartition.regionCount
            logger.info(s"Saving to ${higherHHTables.descendLimit.baseTableRow.tableName}")
            regularPartition.regions.zipWithIndex.foreach {
                case (reg, idx) => {
                    logger.info(s"Processing region $idx/$totalRegs")
                    database.withTransaction { implicit s: Session =>
                        val grp = {
                            val grphConf = dbGraphConfLens.get(parsedConfig)
                            val ret = new HHDatabaseGraph(hhTables, roadNetTables, None, grphConf.effectiveNodeCacheSize, s)
                            grphConf.preload(ret)
                            ret
                        }

                        val initial = {
                            val initialQ = (roadNetTables.roadNodes leftJoin higherHHTables.descendLimit).on(_.id === _.nodeId).filter {
                                case (nd, _) => nd.geom @&& reg.withBuffer(bufferSize).dbBBox
                            }.map {
                                case (nd, dl) => nd.id -> dl.descendLimit.?
                            }
                            logger.debug(initialQ.selectStatement)
                            initialQ.toMap
                        }

                        val higherNodes = higherRoadNetTables.roadNodes.filter(_.geom @&& reg.withBuffer(bufferSize).dbBBox).map(_.id).buildColl[Set]

                        val result = {
                            val nodes = roadNetTables.roadNodes.filter(_.geom @&& reg.dbBBox).map(_.id).iterator
                            getDescendLimits(grp, grp, grp, initial.mapValues(_.getOrElse(0.0)), nodes)
                        }

                        val toIns = result.filter {
                            case (nd, _) => initial(nd).isEmpty && higherNodes.contains(nd)
                        }
                        logger.info(s"Inserting ${toIns.size}")
                        val toUpd = result.filter {
                            case (nd, res) if higherNodes.contains(nd) => initial(nd) match {
                                case Some(initRes) if initRes < res => true
                                case _ => false
                            }
                            case _ => false
                        }
                        logger.info(s"Updating ${toUpd.size}")
                        higherHHTables.descendLimit.insertAll(toIns.toSeq: _*)
                        toUpd.foreach {
                            case (nd, res) => higherHHTables.descendLimit.filter(_.nodeId === nd).map(_.descendLimit).update(res)
                        }
                    }
                }
            }
        }
    }
}