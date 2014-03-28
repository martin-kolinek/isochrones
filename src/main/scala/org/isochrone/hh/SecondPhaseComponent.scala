package org.isochrone.hh

import scala.collection.immutable.Queue
import scala.annotation.tailrec
import org.isochrone.graphlib.GraphComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.graphlib.GraphType
import com.typesafe.scalalogging.slf4j.Logging

trait SecondPhaseComponent {
    self: FirstPhaseComponent with GraphComponentBase =>

    def secondPhase(graph: GraphType[NodeType], neighSizes: NeighbourhoodSizes[NodeType]) = new SecondPhase(graph, neighSizes)

    class SecondPhase(graph: GraphType[NodeType], neighSizes: NeighbourhoodSizes[NodeType]) extends Logging {
        def extractHighwayEdges(tree: NodeTree) = {

            def getPaths(current: NodeType): Seq[List[NodeType]] = {
                tree.childMap.get(current) match {
                    case None => List(List(current))
                    case Some(chlds) => for {
                        c <- chlds
                        sp <- getPaths(c)
                    } yield current :: sp
                }
            }
            val root = tree.parentMap.values.find(x => !tree.parentMap.contains(x)).get
            val paths = getPaths(root)
            def processPath(path: Seq[NodeType]) = {
                val init = List(path.head -> neighSizes.neighbourhoodSize(path.head))
                (init /: path.tail) { (lst, next) =>
                    val (before, beforeCost) = lst.head
                    val nextCost = beforeCost - graph.edgeCost(before, next).get
                    (next, nextCost) :: lst
                }.reverse
            }

            def extractEdges(path: Seq[NodeType]) = {
                val outOfForwNeigh = processPath(path).map(_._2 < 0)
                val outOfRevNeigh = processPath(path.reverse).reverse.map(_._2 < 0)
                val pathWithOutOfNeigh = (path, outOfForwNeigh, outOfRevNeigh).zipped.toSeq
                for {
                    ((a, aFarFromForw, aFarFromBack), (b, bFarFromForw, bFarFromBack)) <- (pathWithOutOfNeigh zip pathWithOutOfNeigh.tail)
                    if aFarFromForw && bFarFromBack || aFarFromBack && bFarFromForw
                } yield (a, b)
            }

            val edges = for {
                p <- paths
                e <- extractEdges(p)
            } yield e
            (edges ++ edges.map(_.swap)).distinct
        }
    }
}