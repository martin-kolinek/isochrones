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
import scala.collection.mutable.HashSet
import com.typesafe.scalalogging.slf4j.Logging

trait LineContractionComponentBase {
    self: GraphComponentBase =>

    trait LineContractionBase {
        val graph: GraphType[NodeType]
        def findEndOfLine(nd: NodeType, prev: NodeType) = {
            @tailrec
            def findEndOfLineInt(nd: NodeType, prev: NodeType, res: List[NodeType], set: Set[NodeType]): Option[List[NodeType]] = {
                val neighs = graph.neighbours(nd)
                val withNd = nd :: res
                if (neighs.size != 2)
                    Some(withNd)
                else {
                    val next = neighs.find(_._1 != prev).get._1
                    if (set.contains(next))
                        None
                    else
                        findEndOfLineInt(next, nd, withNd, set + nd)
                }
            }

            findEndOfLineInt(nd, prev, List(prev), Set())
        }

        case class Line(start: NodeType, inner: List[NodeType], end: NodeType) {
            lazy val nodeSet = Set(start, end)
        }

        def getNodeLine(nd: NodeType) = {
            val neigh = graph.neighbours(nd).head._1
            for {
                toEndPoint1 <- findEndOfLine(neigh, nd)
                line <- findEndOfLine(toEndPoint1.tail.head, toEndPoint1.head)
            } yield {
                val end = toEndPoint1.head
                val start = line.head
                val inner = line.tail.dropRight(1)
                Line(start, inner, end)
            }
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

    type NodeType = Long

    def lineContractor(g: GraphType[NodeType], rnet: RoadNetTables, output: EdgeTable) = new LineContraction(g, rnet, output)

    class LineContraction(g: GraphType[NodeType], rnet: RoadNetTables, output: EdgeTable) extends LineContractionBase with Logging {
        val graph = g
        def contractLines(bbox: Column[Geometry])(implicit s: Session) = {
            val nodesToProcessQuery = for {
                n <- rnet.roadNodes if n.geom @&& bbox
                if Query(rnet.roadNet).filter(e => e.start === n.id).length === 2
            } yield n.id

            val processed = new HashSet[NodeType]
            val total = Query(nodesToProcessQuery.length).first
            nodesToProcessQuery.elements.zipWithIndex.foreach {
                case (nd, idx) => {
                    if (!processed.contains(nd)) {
                        logger.debug(s"Processing $idx/$total")
                        val line = getNodeLine(nd)
                        line.foreach { l =>
                            processed ++= l.inner
                            insertShortcuts(l, s)
                            deleteInner(l, s)
                        }
                    }
                }
            }
        }

        def insertShortcuts(ln: Line, session: Session) = {
            def startEndShortcut(s: NodeType, e: NodeType) = Set(s, e) == Set(ln.start, ln.end)

            for ((s, e, c) <- getShortcuts(ln)) {
                val q = for {
                    n1 <- rnet.roadNodes if n1.id === s
                    n2 <- rnet.roadNodes if n2.id === e
                } yield (n1.id, n2.id, c, false, (n1.geom shortestLine n2.geom).asColumnOf[Geometry])
                if (startEndShortcut(s, e)) {
                    val qWithNonDup = for {
                        edg @ (start, end, _, _, _) <- q
                        if !Query(rnet.roadNet).filter(e => e.start === start && e.end === end).exists
                    } yield edg
                    rnet.roadNet.insert(qWithNonDup)(session)
                } else
                    output.insert(q)(session)
            }
        }

        def deleteInner(ln: Line, s: Session) {
            def delEdge(a: NodeType, b: NodeType) {
                rnet.roadNet.filter(e => e.start === a && e.end === b).delete(s)
            }

            for (Seq(a, b) <- (ln.start +: ln.inner :+ ln.end).sliding(2)) {
                delEdge(a, b)
                delEdge(b, a)
            }
        }
    }
}