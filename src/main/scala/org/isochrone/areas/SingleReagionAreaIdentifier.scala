package org.isochrone.areas

import scala.annotation.tailrec
import org.isochrone.graphlib.GraphType
import org.isochrone.util._
import org.isochrone.graphlib.NodePositionComponent
import org.isochrone.graphlib.GraphComponent
import com.typesafe.scalalogging.slf4j.Logging

trait SingleReagionAreaIdentifierComponent extends AreaComponent {
    self: NodePositionComponent with GraphComponent =>

    object SingleRegionAreaFinder extends Logging {

        type DoneEdgesSet = Set[(NodeType, NodeType)]

        @tailrec
        def completeArea(part: List[NodeType], firstNode: NodeType, done: DoneEdgesSet): (Area, DoneEdgesSet) = {
            val (last :: before :: _) = part
            val (lx, ly) = nodePos.nodePosition(last)
            val (bx, by) = nodePos.nodePosition(before)
            val x = bx - lx
            val y = by - ly

            val lastAngle = math.atan2(y, x)
            logger.debug(s"Investigating neighbours of $last ($lx, $ly), b = $before ($bx, $by), with b - l = ($x, $y), lastAngle = $lastAngle")
            val next = graph.neighbours(last).map(_._1).filter(x => !done.contains(last -> x)).minBy {
                neigh =>
                    val (nx, ny) = nodePos.nodePosition(neigh)
                    val xx = nx - lx
                    val yy = ny - ly
                    val newAngle = math.atan2(yy, xx)
                    val ret = normalizeAngle(newAngle - lastAngle)
                    logger.debug(s"Neighbour $neigh ($nx, $ny), with n - l = ($xx, $yy), newAngle = $newAngle, diff = $ret")
                    if (neigh == before)
                        Double.MaxValue
                    else
                        ret

            }
            logger.debug(s"Picked $next")
            if (next == firstNode) {
                logger.debug(s"Completed area $part")
                (Area(part), done + (last -> next))
            } else
                completeArea(next :: part, firstNode, done + (last -> next))
        }

        @tailrec
        def startingEdgesAreas(edges: List[(NodeType, NodeType)], done: DoneEdgesSet, current: List[Area]): (List[Area], DoneEdgesSet) = {
            edges match {
                case Nil => current -> done
                case (edge@(start, end)) :: tail => {
                    if (done.contains(edge))
                        startingEdgesAreas(tail, done, current)
                    else {
                        val (area, newDone) = completeArea(List(end, start), start, done + (start -> end))
                        startingEdgesAreas(tail, newDone, area :: current)
                    }

                }
            }
        }

        def nodeAreas(nd: NodeType, done: DoneEdgesSet): (List[Area], DoneEdgesSet) = {
            val edges = graph.neighbours(nd).map(x => nd -> x._1).toList
            startingEdgesAreas(edges, done, Nil)
        }

        @tailrec
        def areasForNodes(nds: List[NodeType], done: DoneEdgesSet, current: List[Area]): List[Area] = {
            nds match {
                case Nil => current
                case head :: tail => {
                    val (ndArs, newDone) = nodeAreas(head, done)
                    areasForNodes(tail, newDone, ndArs ++ current)
                }
            }
        }

        def findAreas() = areasForNodes(graph.nodes.toList, Set(), Nil)

    }
}