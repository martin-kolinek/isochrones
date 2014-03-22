package org.isochrone.hh

import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.graphlib.GraphType
import scala.collection.mutable.HashMap
import org.isochrone.graphlib.MapGraphType
import com.typesafe.scalalogging.slf4j.Logging

trait QueryGraphComponent {
    self: GraphComponentBase =>

    case class NodeWithLevel(nd: NodeType, level: Int)

    implicit val NodeWithLevelOrdering = Ordering[(Int, Int)].on[NodeWithLevel](x => -x.level -> x.nd.hashCode)

    class QueryGraph(levels: IndexedSeq[GraphType[NodeType] with HHProps[NodeType]],
                     shortcuts: IndexedSeq[GraphType[NodeType]],
                     reverseShortcuts: IndexedSeq[GraphType[NodeType]],
                     limit: Double) extends MapGraphType[NodeWithLevel] with Logging {
        logger.debug(s"Creating QueryGraph (levels = $levels)")
        def neighbours(nodeWithLevel: NodeWithLevel) = {
            logger.debug(s"neighbours of $nodeWithLevel")
            def withLevel(n: (NodeType, Double)) = NodeWithLevel(n._1, nodeWithLevel.level) -> n._2

            val g = levels(nodeWithLevel.level)
            val sameLevel = {
                g.neighbours(nodeWithLevel.nd).filter(isLeavingEntranceNeighbourhood(nodeWithLevel)).map(withLevel)
            }
            val toUpperLevel = {
                if (g.hasHigherLevel(nodeWithLevel.nd))
                    List(NodeWithLevel(nodeWithLevel.nd, (nodeWithLevel.level + 1)) -> 0.0)
                else
                    Nil
            }
            val toLowerLevel = {
                if (closedCost(nodeWithLevel) + g.descendLimit(nodeWithLevel.nd) > limit && nodeWithLevel.level > 0)
                    List(nodeWithLevel.copy(level = nodeWithLevel.level - 1) -> 0.0)
                else
                    Nil
            }
            val shortcutEdges = {
                shortcuts(nodeWithLevel.level).neighbours(nodeWithLevel.nd).map(withLevel)
            }
            val reverseShortcutEdges = {
                if (closedCost(nodeWithLevel) + g.shortcutReverseLimit(nodeWithLevel.nd) > limit)
                    reverseShortcuts(nodeWithLevel.level).neighbours(nodeWithLevel.nd).map(withLevel)
                else
                    Nil
            }
            logger.debug(s"sameLevel: $sameLevel")
            logger.debug(s"toUpperLevel: $toUpperLevel")
            logger.debug(s"toLowerLevel: $toLowerLevel")
            logger.debug(s"shortcutEdges: $shortcutEdges")
            logger.debug(s"reverseShortcutEdges: $reverseShortcutEdges")
            (sameLevel ++ toUpperLevel ++ toLowerLevel ++ shortcutEdges ++ reverseShortcutEdges).toMap
        }

        def nodes = for {
            (g, l) <- levels.zipWithIndex
            n <- g.nodes
        } yield NodeWithLevel(n, l)

        val previousEntranceNodes = new HashMap[NodeWithLevel, NodeWithLevel]

        val closedCosts = new HashMap[NodeWithLevel, Double]

        def isLeavingEntranceNeighbourhood(nd: NodeWithLevel)(neigh: (NodeType, Double)): Boolean = {
            val lastEntrance = previousEntranceNodes(nd)
            val entranceCost = closedCost(lastEntrance)
            val fromEntrance = (closedCost(nd) - entranceCost) + neigh._2
            val entranceNeigh = levels(lastEntrance.level).neighbourhoodSize(lastEntrance.nd)
            val ndReverseNeigh = levels(nd.level).reverseNeighSize(neigh._1)
            logger.debug(s"isLeavingEntranceNeigh(${neigh._1})")
            logger.debug(s"last entrance = $lastEntrance")
            logger.debug(s"fromEntrance: $fromEntrance")
            logger.debug(s"entranceNeigh: $entranceNeigh")
            logger.debug(s"ndReverseNeigh: $ndReverseNeigh")
            fromEntrance < entranceNeigh + ndReverseNeigh
        }

        def closedCost(nd: NodeWithLevel): Double = closedCosts(nd)

        def onClosed(closed: NodeWithLevel, closedCost: Double, previous: Option[(NodeWithLevel, Double)]) {
            closedCosts(closed) = closedCost
            previous match {
                case None => previousEntranceNodes(closed) = closed
                case Some((NodeWithLevel(nd, level), _)) if (level != closed.level) ||
                    shortcuts(level).edgeCost(nd, closed.nd).nonEmpty => previousEntranceNodes(closed) = closed
                case Some((prev, _)) if !previousEntranceNodes.contains(closed) => previousEntranceNodes(closed) = previousEntranceNodes(prev)
                case _ => {}
            }
        }

        def onOpened(child: NodeWithLevel, parent: NodeWithLevel, newCost: Double) {
            if (child.level != parent.level || shortcuts(child.level).edgeCost(child.nd, parent.nd).nonEmpty) {
                previousEntranceNodes(child) = child
            } else if (!previousEntranceNodes.contains(child)) {
                previousEntranceNodes(child) = previousEntranceNodes(parent)
            }
        }
    }
}