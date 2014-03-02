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

trait NonDbHigherDescendLimitFinderComponent {
    self: GraphComponentBase with DijkstraAlgorithmProviderComponent =>
    trait NonDbHigherDescendLimitFinder {
        def getDescendLimits(grp: GraphType[NodeType], neighSize: NeighbourhoodSizes[NodeType], descLim: DescendLimitProvider[NodeType], initial: Map[NodeType, Double], nodes: TraversableOnce[NodeType]): Map[NodeType, Double] = {
            def updateDescendLimit(mp: Map[NodeType, Double], start: NodeType): Map[NodeType, Double] = {
                val ns = neighSize.neighbourhoodSize(start)
                val nodes = dijkstraForGraph(grp).helper.compute(start).view.takeWhile(_._2 <= ns)
                val startDescLimit = descLim.descendLimit(start)
                (mp /: nodes) {
                    case (map, (node, fromStart)) => {
                        if (!map.contains(node))
                            throw new Exception("Tried to modify not loaded node descend limit")
                        val current = map(node)
                        map + (node -> math.max(current, fromStart + startDescLimit))
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
            regularPartition.regions.foreach { reg =>
                database.withTransaction { implicit s: Session =>
                    val grp = {
                        val grphConf = dbGraphConfLens.get(parsedConfig)
                        val ret = new HHDatabaseGraph(hhTables, roadNetTables, grphConf.effectiveNodeCacheSize, s)
                        grphConf.preload(ret)
                        ret
                    }
                    val initial = (higherRoadNetTables.roadNodes leftJoin higherHHTables.descendLimit).filter {
                        case (nd, _) => nd.geom @&& reg.withBuffer(bufferSize).dbBBox
                    }.map {
                        case (nd, dl) => nd.id -> dl.descendLimit.?
                    }.toMap
                    val nodes = Query(roadNetTables.roadNodes).filter(_.geom @&& reg.dbBBox).map(_.id).elements

                    val result = getDescendLimits(grp, grp, grp, initial.mapValues(_.getOrElse(0.0)), nodes)

                    val toIns = result.filter {
                        case (nd, _) => initial(nd).isEmpty
                    }

                    val toUpd = result.filter {
                        case (nd, res) => initial(nd) match {
                            case Some(initRes) if initRes < res => true
                            case _ => false
                        }
                    }

                    higherHHTables.descendLimit.insertAll(toIns.toSeq: _*)
                    toUpd.foreach {
                        case (nd, res) => Query(higherHHTables.descendLimit).filter(_.nodeId === nd).map(_.descendLimit).update(res)
                    }
                }
            }
        }
    }
}