package org.isochrone.hh

import org.isochrone.compute.IsochroneComputerComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.dijkstra.DijkstraAlgorithmProviderComponent
import org.isochrone.db.MultiLevelRoadNetTableComponent
import org.isochrone.dbgraph.MultiLevelHHDatabaseGraphComponent
import scala.collection.mutable.ListBuffer
import org.isochrone.dijkstra.GenericDijkstraAlgorithmProvider
import org.isochrone.compute.SomeIsochroneComputerComponent
import scala.collection.mutable.HashSet
import org.isochrone.graphlib.GraphType
import scala.collection.mutable.HashMap
import org.isochrone.util._
import scala.annotation.tailrec
import scala.collection.mutable.MultiMap

trait HHIsochroneComputer extends SomeIsochroneComputerComponent with QueryGraphComponent with GraphComponentBase {
    self: GenericDijkstraAlgorithmProvider with MultiLevelHHDatabaseGraphComponent =>
    type NodeType = Long
    object HHIsoComputer extends IsochroneComputer {
        def isochrone(start: Traversable[(NodeType, Double)], max: Double) = {
            val levels = for {
                ((g, sh), revSh) <- hhDbGraphs zip shortcutGraphs zip reverseShortcutGraphs
                l <- Seq(SearchLevel(sh, revSh, g.neighbourhoodSize, g.reverseNeighSize, g.descendLimit),
                    SearchLevel(g, g, g.neighbourhoodSize, g.reverseNeighSize, g.shortcutReverseLimit))
            } yield l
            work(start, max, Nil, levels.head, levels.tail.toList).map(IsochroneNode.tupled)
        }

        case class SearchLevel(graph: GraphType[NodeType], reverseGraph: GraphType[NodeType], neighSize: NodeType => Double, revNeighSize: NodeType => Double, descendLimit: NodeType => Double)

        case class CurrentLevelResult(toHigher: Traversable[(NodeType, Double)], all: Traversable[(NodeType, Double)], toLower: Traversable[(NodeType, Double)])

        def forwardSearch(start: Traversable[(NodeType, Double)], level: SearchLevel, max: Double) = {
            logger.debug(s"Starting forward search from $start on $level")
            val endCostMap = start.map {
                case (nd, c) => nd -> (c + level.neighSize(nd))
            }.toMap
            logger.debug(s"endCostMap: $endCostMap")

            val startSet = new HashSet[NodeType]
            startSet ++= endCostMap.keys
            val lst = compute(start, level.graph).view.takeWhile { nd =>
                for (start <- nd.start) {
                    if (nd.cost > endCostMap(start))
                        startSet -= start
                }
                startSet.nonEmpty
            }.toList

            val toHigher = lst.filter(n => n.start.forall(start => n.cost <= endCostMap(start))).map(n => (n.nd, n.cost))
            val toLower = lst.filter(n => max - n.cost <= level.descendLimit(n.nd)).map(n => (n.nd, n.cost))
            val all = lst.map(n => (n.nd, n.cost))
            CurrentLevelResult(toHigher, toLower, all)
        }

        def backwardsSearch(start: Traversable[(NodeType, Double)], level: SearchLevel, max: Double) = {
            logger.debug(s"Starting backward search from $start on $level")
            val startMap = start.toMap
            val startSet = new HashSet[NodeType]
            startSet ++= startMap.keys
            val lst = compute(start, level.reverseGraph).view.takeWhile { n =>
                for (start <- n.start) {
                    if (startMap(start) + level.revNeighSize(n.nd) < n.cost)
                        startSet -= start
                }
                logger.debug(s"startSet: $startSet n: $n")
                startSet.nonEmpty
            }.toList
            val toLower = lst.filter(n => max - n.cost <= level.descendLimit(n.nd)).map(n => (n.nd, n.cost))
            val all = lst.map(n => (n.nd, n.cost))
            CurrentLevelResult(Nil, toLower, all)
        }

        def work(start: Traversable[(NodeType, Double)], max: Double, lower: List[SearchLevel], current: SearchLevel, upper: List[SearchLevel]): Traversable[(NodeType, Double)] = {
            val forw = forwardSearch(start, current, max)
            val back = backwardsSearch(forw.all, current, max)
            val fromLower = finishToLower(back, lower, max)
            val fromUpper = upper match {
                case Nil => Nil
                case next :: rest => work(forw.toHigher, max, current :: lower, next, rest)
            }
            fromUpper ++ forw.all ++ back.all ++ fromLower
        }

        @tailrec
        def finishToLower(fromUpper: CurrentLevelResult, lower: List[SearchLevel], max: Double): Traversable[(NodeType, Double)] = lower match {
            case Nil => fromUpper.all
            case current :: rest => {
                val curResult = backwardsSearch(fromUpper.toLower, current, max)
                finishToLower(CurrentLevelResult(Nil, curResult.toLower, curResult.all ++ fromUpper.all), rest, max)
            }
        }

        case class NodeWithStart(nd: NodeType, cost: Double, start: Set[NodeType])

        def compute(start: Traversable[(NodeType, Double)], g: GraphType[NodeType]) = {
            val dijk = dijkstraForGraph(g)
            val startMap = new HashMap[NodeType, scala.collection.mutable.Set[NodeType]] with MultiMap[NodeType, NodeType]
            for ((n, _) <- start) {
                startMap.addBinding(n, n)
            }
            new Traversable[NodeWithStart] {
                def foreach[U](f: NodeWithStart => U) = {
                    dijk.alg(start,
                        (nd, cst, _) => f(NodeWithStart(nd, cst, startMap(nd).toSet)),
                        (child, parent, _) => startMap(parent).foreach(s => startMap.addBinding(child, s)),
                        () => false)
                }
            }
        }
    }

    val isoComputer = HHIsoComputer
}