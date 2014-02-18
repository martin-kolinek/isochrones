package org.isochrone.hh

import org.isochrone.graphlib.GraphComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.db.HigherLevelRoadNetTableComponent
import org.isochrone.dijkstra.DijkstraAlgorithmComponent
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import org.isochrone.graphlib.GraphType
import org.isochrone.dijkstra.DijkstraAlgorithmProviderComponent
import org.isochrone.OptionParserComponent
import org.isochrone.ArgumentParser
import scopt.OptionParser

trait FirstPhaseComponent {
    self: GraphComponentBase =>

    case class NodeTree(childMap: collection.immutable.Map[NodeType, Seq[NodeType]], parentMap: collection.immutable.Map[NodeType, NodeType], withinStartNeighbourhood: collection.immutable.Set[NodeType])

    def firstPhase(g: GraphType[NodeType], neigh: NeighbourhoodSizes[NodeType]): FirstPhase

    trait FirstPhase {
        def nodeTree(start: NodeType): NodeTree
    }
}

trait FirstPhaseComponentImpl extends FirstPhaseComponent {
    self: DijkstraAlgorithmProviderComponent with GraphComponentBase with FirstPhaseParameters =>

    def firstPhase(g: GraphType[NodeType], neigh: NeighbourhoodSizes[NodeType]) = new FirstPhaseImpl {
        val graph = g
        val neighbourhoods = neigh
    }

    trait FirstPhaseImpl extends FirstPhase {
        val graph: GraphType[NodeType]
        val neighbourhoods: NeighbourhoodSizes[NodeType]

        def nodeTree(start: NodeType): NodeTree = {
            val startDh = neighbourhoods.neighbourhoodSize(start)
            val activeNodes = new HashSet[NodeType]
            activeNodes += start
            val activeUnsettledNonMaverick = new HashSet[NodeType]
            activeUnsettledNonMaverick += start

            val s1Rest = new HashMap[NodeType, Double]
            val predecessors = new HashMap[NodeType, NodeType]
            val uCosts = new HashMap[NodeType, Double]
            val withinStartNeighbourhood = new HashSet[NodeType]

            def cancel() = activeUnsettledNonMaverick.isEmpty

            def isMaverick(fromStart: Double) = fromStart > maverickThresholdMultiplier * startDh

            def opened(node: NodeType, parent: NodeType, fromStart: Double) {
                if (activeNodes.contains(parent)) {
                    activeNodes += node
                    if (!isMaverick(fromStart))
                        activeUnsettledNonMaverick += node
                }
            }

            def closed(current: NodeType, costFromStart: Double, previous: Option[(NodeType, Double)]) = {
                activeUnsettledNonMaverick -= current
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

            dijkstraForGraph(graph).alg(Seq(start -> 0), closed, opened, cancel)

            val childMap = predecessors.toSeq.view.map(_.swap).groupBy(_._1).map {
                case (par, chlds) => par -> chlds.map(_._2).force
            }
            NodeTree(childMap, predecessors.toMap, withinStartNeighbourhood.toSet)
        }
    }
}

trait FirstPhaseParameters {
    def maverickThresholdMultiplier: Double
}

trait FirstPhaseParametersFromArg extends OptionParserComponent with FirstPhaseParameters {
    self: ArgumentParser =>

    lazy val maverickThresMultLens = registerConfig(6.0)

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) {
        super.parserOptions(pars)
        pars.opt[Double]("maverick-multiplier").text("Maverick threshold multiplier for highway hierarchies (default = 6)").
            action((x, c) => maverickThresMultLens.set(c)(x))
    }

    lazy val maverickThresholdMultiplier = maverickThresMultLens.get(parsedConfig)

}