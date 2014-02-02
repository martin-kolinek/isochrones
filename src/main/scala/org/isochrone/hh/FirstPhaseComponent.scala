package org.isochrone.hh

import org.isochrone.graphlib.GraphComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.db.HigherLevelRoadNetTableComponent
import org.isochrone.dijkstra.DijkstraAlgorithmComponent
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap

trait FirstPhaseComponent {
    self: NeighbourhoodSizeComponent with GraphComponent with DijkstraAlgorithmComponent =>

    case class NodeTree(childMap: collection.immutable.Map[NodeType, Seq[NodeType]], parentMap: collection.immutable.Map[NodeType, NodeType], withinStartNeighbourhood: collection.immutable.Set[NodeType])

    object FirstPhase {
        def nodeTree(start: NodeType): NodeTree = {
            val startDh = neighbourhoods.neighbourhoodSize(start)
            val activeNodes = new HashSet[NodeType]
            activeNodes += start
            val activeUnsettled = new HashSet[NodeType]
            activeUnsettled += start

            val s1Rest = new HashMap[NodeType, Double]

            val predecessors = new HashMap[NodeType, NodeType]

            val uCosts = new HashMap[NodeType, Double]

            val withinStartNeighbourhood = new HashSet[NodeType]

            def cancel() = activeUnsettled.isEmpty

            def opened(node: NodeType, parent: NodeType) {
                if (activeNodes.contains(parent)) {
                    activeNodes += node
                    activeUnsettled += node
                }
            }

            def closed(current: NodeType, costFromStart: Double, previous: Option[(NodeType, Double)]) = {
                activeUnsettled -= current
                if (costFromStart < startDh)
                    withinStartNeighbourhood += current
                previous match {
                    case None => {}
                    case Some((prev, _)) if prev == start => {
                        s1Rest(current) = neighbourhoods.neighbourhoodSize(current)
                    }
                    case Some((prev, prevCost)) => {
                        predecessors(current) = prev
                        val prevRest = s1Rest(prev)
                        val rest = prevRest - prevCost
                        if (rest < 0 && prevRest >= 0) {
                            uCosts(prev) = 0
                        }
                        uCosts.get(prev).foreach { prevUCost =>
                            val backEdgeCost = graph.edgeCost(current, prev).getOrElse({ throw new Exception(s"No backward edge $current -> $prev") })
                            val curUCost = prevUCost + backEdgeCost
                            uCosts(current) = curUCost
                            if (curUCost > neighbourhoods.neighbourhoodSize(current))
                                activeNodes -= current

                        }
                        s1Rest(current) = rest
                    }
                }
            }

            DijkstraAlgorithm.alg(Seq(start -> 0), closed, opened, cancel)

            val childMap = predecessors.toSeq.view.map(_.swap).groupBy(_._1).map {
                case (par, chlds) => par -> chlds.map(_._2).force
            }
            NodeTree(childMap, predecessors.toMap, withinStartNeighbourhood.toSet)
        }
    }
}