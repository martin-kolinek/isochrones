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
import scala.annotation.tailrec

trait HHIsochroneComputer extends SomeIsochroneComputerComponent with QueryGraphComponent with GraphComponentBase {
    self: GenericDijkstraAlgorithmProvider with MultiLevelHHDatabaseGraphComponent =>
    type NodeType = Long
    object HHIsoComputer extends IsochroneComputer {
        def isochrone(start: Traversable[(NodeType, Double)], max: Double) = {

            ???
        }

        case class SearchLevel(graph: GraphType[NodeType], reverseGraph: GraphType[NodeType], neighSize: NodeType => Double, revNeighSize: NodeType => Double, descendLimit: NodeType => Double)

        case class CurrentLevelResult(toHigher: Traversable[(NodeType, Double)], all: Traversable[(NodeType, Double)], toLower: Traversable[(NodeType, Double)])

        def traverseGraph(start: Traversable[(NodeType, Double)], level: SearchLevel, max: Double) = {
            val endCostMap = start.map {
                case (nd, c) => nd -> (c + level.neighSize(nd))
            }.toMap

            val startSet = new HashSet[NodeType]
            startSet ++= endCostMap.keys
            val lst = compute(start, level.graph).view.takeWhile { nd =>
                if (nd.cost > endCostMap(nd.start))
                    startSet -= nd.nd
                startSet.nonEmpty
            }.toList

            val toHigher = lst.filter(n => n.cost <= endCostMap(n.nd)).map(n => (n.nd, n.cost))
            val toLower = lst.filter(n => max - n.cost <= level.descendLimit(n.nd)).map(n => (n.nd, n.cost))
            val all = lst.map(n => (n.nd, n.cost))
            CurrentLevelResult(toHigher, toLower, all)
        }

        def continueToLower(start: Traversable[(NodeType, Double)], level: SearchLevel, max: Double) = {
            val startMap = start.toMap
            val startSet = new HashSet[NodeType]
            startSet ++= startMap.keys
            val lst = compute(start, level.reverseGraph).view.takeWhile { n =>
                if (startMap(n.start) + level.revNeighSize(n.nd) < n.cost)
                    startSet -= n.nd
                startSet.nonEmpty
            }.toList
            val toLower = lst.filter(n => max - n.cost <= level.descendLimit(n.nd)).map(n => (n.nd, n.cost))
            val all = lst.map(n => (n.nd, n.cost))
            CurrentLevelResult(Nil, toLower, all)
        }

        def work(start: Traversable[(NodeType, Double)], max: Double, lower: List[SearchLevel], current: SearchLevel, upper: List[SearchLevel]): Traversable[(NodeType, Double)] = upper match {
            case Nil => Nil
            case next :: rest => {
                val res = traverseGraph(start, current, max)
                val lowerRes = continueToLower(res.all, current, max)
                val fromLower = finishToLower(lowerRes, lower, max)
                val fromUpper = work(res.toHigher, max, current :: lower, next, rest)
                fromUpper ++ fromLower
            }
        }

        @tailrec
        def finishToLower(fromUpper: CurrentLevelResult, lower: List[SearchLevel], max: Double): Traversable[(NodeType, Double)] = lower match {
            case Nil => fromUpper.all
            case current :: rest => {
                val curResult = continueToLower(fromUpper.toLower, current, max)
                finishToLower(CurrentLevelResult(Nil, curResult.toLower, curResult.all ++ fromUpper.all), rest, max)
            }
        }

        case class NodeWithStart(nd: NodeType, cost: Double, start: NodeType)

        def compute(start: Traversable[(NodeType, Double)], g: GraphType[NodeType]) = {
            val dijk = dijkstraForGraph(g)
            val startMap = new HashMap[NodeType, NodeType]
            new Traversable[NodeWithStart] {
                def foreach[U](f: NodeWithStart => U) = {
                    dijk.alg(start, {
                        case (nd, cst, None) => {
                            startMap(nd) = nd
                            f(NodeWithStart(nd, cst, nd))
                        }
                        case (nd, cst, Some((prev, _))) => {
                            val start = startMap(prev)
                            startMap(nd) = start
                            f(NodeWithStart(nd, cst, start))
                        }
                    }, (_, _, _) => {}, () => false)
                }
            }
        }
    }

    val isoComputer = HHIsoComputer
}