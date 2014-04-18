package org.isochrone.areas

import scala.annotation.tailrec
import org.isochrone.graphlib.GraphWithRegionsComponent
import org.isochrone.graphlib.NodePositionComponent
import org.isochrone.util._
import org.isochrone.graphlib.GraphWithRegionsType
import org.isochrone.graphlib.NodePosition
import org.isochrone.graphlib.GraphWithRegionsType
import org.isochrone.graphlib.GraphWithRegionsComponent
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.graphlib.GraphType
import org.isochrone.graphlib.GraphComponent

trait AreaIdentifierComponent extends AreaComponent {
    self: NodePositionComponent with GraphWithRegionsComponent =>

    object AreaIdentifier extends Logging {

        class DoneEdgesSet(private val doneEdges: Set[(NodeType, NodeType)], private val doneRegions: Set[RegionType], currentRegion: Option[RegionType]) {
            def this() = this(Set(), Set(), None)

            def withEdge(edg: (NodeType, NodeType)) = new DoneEdgesSet(doneEdges + edg, doneRegions, currentRegion)
            def finishRegion(reg: RegionType) = new DoneEdgesSet(Set(), doneRegions + reg, currentRegion)

            def contains(edg: (NodeType, NodeType)) = {
                doneEdges.contains(edg)
            }

            def regionDone(nd: NodeType) = graph.nodeRegion(nd).map(doneRegions.contains).getOrElse(true)

            def withCurrentRegion(r: RegionType) = new DoneEdgesSet(doneEdges, doneRegions, Some(r))
        }

        @tailrec
        def completeArea(part: List[NodeType], firstEdge: (NodeType, NodeType), done: DoneEdgesSet): (Option[Area], DoneEdgesSet) = {
            val (last :: (before :: _)) = part
            val (lx, ly) = nodePos.nodePosition(last)
            val (bx, by) = nodePos.nodePosition(before)
            val x = bx - lx
            val y = by - ly

            val lastAngle = math.atan2(y, x)
            logger.debug(s"Investigating neighbours of $last ($lx, $ly), b = $before ($bx, $by), with b - l = ($x, $y), lastAngle = $lastAngle")
            val filtered = graph.neighbours(last).map(_._1).filter(x => !done.contains(last -> x) || last -> x == firstEdge)
            val next =
                if (filtered.isEmpty) None
                else Some(filtered.minBy {
                    neigh =>
                        val (nx, ny) = nodePos.nodePosition(neigh)
                        val xx = nx - lx
                        val yy = ny - ly
                        val newAngle = math.atan2(yy, xx)
                        val ret = normalizeAngle(lastAngle - newAngle)
                        logger.debug(s"Neighbour $neigh ($nx, $ny), with n - l = ($xx, $yy), newAngle = $newAngle, diff = $ret")
                        if (neigh == before)
                            Double.MaxValue
                        else
                            ret
                })
            logger.debug(s"Picked $next")
            if (next.map(done.regionDone).getOrElse(true)) {
                logger.debug(s"Abort area")
                (None, done)
            } else if ((last, next.get) == firstEdge) {
                logger.debug(s"Completed area")
                if (part.tail.size <= 2)
                    throw new Exception(s"Area with size <= 2: $part")
                (Some(Area(part.tail)), done.withEdge(last -> next.get))
            } else
                completeArea(next.get :: part, firstEdge, done.withEdge(last -> next.get))
        }

        @tailrec
        def startingEdgesAreas(edges: List[(NodeType, NodeType)], done: DoneEdgesSet, current: List[Area]): (List[Area], DoneEdgesSet) = {
            edges match {
                case Nil => current -> done
                case (edge@(start, end)) :: tail => {
                    if (done.contains(edge))
                        startingEdgesAreas(tail, done, current)
                    else {
                        val (area, newDone) = completeArea(List(end, start), edge, done.withEdge(start -> end))
                        area match {
                            case None => startingEdgesAreas(tail, done, current)
                            case Some(ar) => startingEdgesAreas(tail, newDone, ar :: current)
                        }
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

        def getAreasForRegions(rgs: List[RegionType], last: Seq[Area], done: DoneEdgesSet): (List[RegionType], Seq[Area], DoneEdgesSet) = {
            val rg = rgs.head
            logger.info(s"Computing areas for region $rg")
            val nodes = graph.singleRegion(rg).nodes.toList
            logger.debug(s"Nodes for region: $nodes")
            val areas = areasForNodes(nodes, done.withCurrentRegion(rg), Nil)
            (rgs.tail, areas, done.finishRegion(rg))
        }

        def allAreas = {
            val regs = graph.regions.toList
            Stream.iterate((regs, Seq[Area](), new DoneEdgesSet()))((getAreasForRegions _).tupled).
                take(regs.size + 1).
                flatMap(_._2)
        }
    }
}
