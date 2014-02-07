package org.isochrone.hh

import org.isochrone.db.RegularPartitionComponent
import org.isochrone.graphlib.GraphType
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.db.RoadNetTables
import org.isochrone.util.db.MyPostgresDriver.simple._
import scala.annotation.tailrec
import com.vividsolutions.jts.geom.Geometry
import org.isochrone.graphlib.GraphComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.EdgeTable

trait LineContractionComponentBase {
    self: GraphComponent =>

    trait LineContractionBase {

        def findEndOfLine(nd: NodeType, prev: NodeType) = {
            @tailrec
            def findEndOfLineInt(nd: NodeType, prev: NodeType, res: List[NodeType]): List[NodeType] = {
                val neighs = graph.neighbours(nd)
                val withNd = nd :: res
                if (neighs.size != 2)
                    withNd
                else {
                    val next = neighs.find(_._1 != prev).get._1
                    findEndOfLineInt(next, nd, withNd)
                }
            }

            findEndOfLineInt(nd, prev, Nil)
        }

        case class Line(start: NodeType, inner: List[NodeType], end: NodeType)

        def getNodeLine(nd: NodeType) = {
            val neigh = graph.neighbours(nd).head._1
            val toEndPoint1 = findEndOfLine(neigh, nd)
            val line = findEndOfLine(toEndPoint1.tail.head, toEndPoint1.head)
            val end = toEndPoint1.head
            val start = line.head
            val inner = line.tail
            Line(start, inner, end)
        }

        def getShortcuts(ln: Line) = {
            def ammendWithCosts(end: NodeType, inner: List[NodeType]) = {
                (inner :\ List(end -> 0.0)) { (nd, l) =>
                    val (prev, prevTotal) = l.head
                    val total = prevTotal + graph.edgeCost(prev, nd).get
                    nd -> total :: l
                }
            }

            def getOneWayShortcuts(start: NodeType, inner: List[NodeType], end: NodeType) = {
                val withCosts = ammendWithCosts(end, start :: inner)
                withCosts.view.filterNot(_._1 == end).map {
                    case (nd, cost) => (nd, end, cost)
                }.force
            }

            getOneWayShortcuts(ln.start, ln.inner, ln.end) ++ getOneWayShortcuts(ln.end, ln.inner.reverse, ln.start)
        }

    }
}

trait LineContractionComponent extends GraphComponentBase with LineContractionComponentBase {
    self: RegularPartitionComponent with GraphComponent with RoadNetTableComponent =>

    type NodeType = Long

    class LineContraction(output: EdgeTable) extends LineContractionBase {
        def contractLines(bbox: regularPartition.BoundingBox) = {
            val nodesToProcessQuery = for {
                n <- roadNetTables.roadNodes if n.geom @&& bbox.dbBBox
                if Query(roadNetTables.roadNet).filter(e => e.start === n.id).length === 2
            } yield n.id
        }

        def insertShortcuts(ln: Line, session: Session) = {
            for ((s, e, c) <- getShortcuts(ln)) {
                val q = for {
                    n1 <- roadNetTables.roadNodes if n1.id === s
                    n2 <- roadNetTables.roadNodes if n2.id === e
                } yield (n1.id, n2.id, c, false, (n1.geom shortestLine n2.geom).asColumnOf[Geometry])
                output.insert(q)(session)
            }
        }

        def deleteInner(ln: Line, s: Session) {
            def delEdge(a: NodeType, b: NodeType) {
                roadNetTables.roadNet.filter(e => e.start === a && e.end === b).delete(s)
            }

            for (Seq(a, b) <- ln.inner.sliding(2)) {
                delEdge(a, b)
                delEdge(b, a)
            }
        }
    }
}