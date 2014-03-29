package org.isochrone.hh

import org.isochrone.db.RegularPartitionComponent
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.db.DatabaseProvider
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.BufferOptionParserComponent
import org.isochrone.ArgumentParser
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.dbgraph.HHDatabaseGraph
import org.isochrone.dbgraph.DBGraphConfigParserComponent
import org.isochrone.dijkstra.DijkstraAlgorithmProviderComponent

trait ReverseNeighbourhoodFinderComponent extends Logging with BufferOptionParserComponent with DBGraphConfigParserComponent {
    self: RegularPartitionComponent with DatabaseProvider with RoadNetTableComponent with HHTableComponent with ArgumentParser with DijkstraAlgorithmProviderComponent =>

    type NodeType = Long

    object ReverseNeighbourhoodFinder {
        def findReverseNeighbourhoods() {
            logger.info("Finding reverse neighbourhood sizes")
            val total = regularPartition.regionCount
            val bufferSize = bufferSizeLens.get(parsedConfig)
            regularPartition.regions.zipWithIndex.foreach {
                case (reg, i) => {
                    logger.info(s"Processing $i/$total")
                    database.withTransaction { implicit s =>
                        val initial = {
                            val initialQ = (roadNetTables.roadNodes leftJoin hhTables.reverseNeighbourhoods on (_.id === _.nodeId)).filter {
                                case (nd, _) => nd.geom @&& reg.withBuffer(bufferSize).dbBBox
                            }.map {
                                case (nd, rn) => nd.id -> rn.neighbourhood.?
                            }
                            initialQ.toMap
                        }

                        val nodes = {
                            val q = roadNetTables.roadNodes.filter(_.geom @&& reg.dbBBox).map(_.id)
                            q.list
                        }

                        val graph = {
                            val grphConf = dbGraphConfLens.get(parsedConfig)
                            val gr = new HHDatabaseGraph(hhTables, roadNetTables, None, grphConf.effectiveNodeCacheSize, s)
                            grphConf.preload(gr)
                            gr
                        }

                        val dijk = dijkstraForGraph(graph)

                        def findRevNeighbourhoods(nodes: Traversable[NodeType], initial: Map[NodeType, Option[Double]]) = {
                            def updateMap(map: Map[NodeType, Option[Double]], nd: NodeType) = {
                                (map /: dijk.helper.nodesWithin(nd, graph.neighbourhoodSize(nd))) {
                                    case (oldMap, (other, fromStart)) => {
                                        if (!oldMap.contains(other))
                                            throw new Exception(s"$other not in initial")
                                        val newVal = oldMap(other) match {
                                            case None => Some(fromStart)
                                            case Some(oldVal) if oldVal < fromStart => Some(fromStart)
                                            case old => old
                                        }

                                        oldMap.updated(other, newVal)
                                    }
                                }
                            }

                            (initial /: nodes)(updateMap)
                        }

                        val updated = findRevNeighbourhoods(nodes, initial)

                        val toIns = updated.collect {
                            case (nd, Some(value)) if initial(nd).isEmpty => nd -> value
                        }.toSeq

                        logger.info(s"Inserting ${toIns.size}")
                        val toUpd = updated.collect {
                            case (nd, Some(value)) if initial(nd).isDefined && initial(nd).get < value => nd -> value
                        }.toSeq
                        logger.info(s"Updating ${toUpd.size}")

                        hhTables.reverseNeighbourhoods.insertAll(toIns: _*)
                        for ((nd, c) <- toUpd) {
                            hhTables.reverseNeighbourhoods.filter(_.nodeId === nd).map(_.neighbourhood).update(c)
                        }
                    }
                }
            }
        }
    }
}