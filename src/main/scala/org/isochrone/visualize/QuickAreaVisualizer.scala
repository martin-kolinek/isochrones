package org.isochrone.visualize

import org.isochrone.graphlib.GraphComponentBase
import spire.std.double._
import spire.std.seq._
import spire.syntax.normedVectorSpace._
import org.isochrone.util._
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.PrecisionModel
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.LineString
import com.vividsolutions.jts.geom.Point

trait QuickAreaVisualizerComponent extends AreaVisualizerComponentTypes with CircleDrawingComponent {
    self: GraphComponentBase with CirclePointsCountComponent with AzimuthalProjectionComponent =>

    val geomFact = new GeometryFactory(new PrecisionModel, 4326)

    import ListPositionImplicit._

    class AreaVisGeom(area: PosArea, areaGeom: Geometry, nodes: List[IsochroneNode]) {
        val nodeMap = nodes.map(x => x.nd -> x.remaining).toMap
        val closedArea = area.points :+ area.points.head
        val edges = closedArea.sliding(2).toList

        case class ResultPoint(pt: Position, onEdge: Boolean)

        def interiorPoint(p: Position) = ResultPoint(p, false)

        def createLine(start: PointWithPosition, end: PointWithPosition) = {
            val cost = area.cost(start.nd, end.nd)

            def tangentPoints(pt: Position, remaining: Double, other: Position) = {
                val center = (pt + other) :/ 2.0
                val proj = projectionForPoint(pt.x, pt.y)
                val projectedCenter = vector.tupled(proj.project(center.x, center.y))
                val centerRad = projectedCenter.norm
                VisualizationUtil.circleIntersection(vector(0, 0), projectedCenter, remaining, centerRad).
                    map(x => vector.tupled(proj.unproject(x.x, x.y))).
                    map(interiorPoint)
            }

            val startProj = projectionForPoint(start.pos.x, start.pos.y)

            (nodeMap.get(start.nd), nodeMap.get(start.nd).map(_ - cost).filter(_ > 0)) match {
                case (None, _) => None
                case (Some(remaining), None) => {
                    val endPoint = ResultPoint(start.pos + ((end.pos - start.pos) :* (remaining / cost)), true)
                    val startPoints = tangentPoints(start.pos, remaining, endPoint.pt)
                    Some(startPoints, Seq(endPoint, endPoint))
                }
                case (Some(startRem), Some(endRem)) => {
                    val startPoints = tangentPoints(start.pos, startRem, end.pos)
                    val endPoints = tangentPoints(end.pos, endRem, start.pos).reverse
                    Some(startPoints, endPoints)
                }
            }
        }

        def extractForward(starts: Seq[ResultPoint], ends: Seq[ResultPoint]) = (starts.tail.head, ends.tail.head)
        def extractBack(starts: Seq[ResultPoint], ends: Seq[ResultPoint]) = (ends.head, starts.head)

        def connectAroundNode(node: Position)(firstStart: ResultPoint, firstEnd: ResultPoint)(secondStart: ResultPoint, secondEnd: ResultPoint): List[ResultPoint] = {
            val firstAng = (firstStart.pt - firstEnd.pt).angle
            val secondAng = (secondEnd.pt - secondStart.pt).angle
            val diff = normalizeAngle(secondAng - firstAng)
            if (diff > math.Pi) {
                val radius = (firstEnd.pt - node).norm
                val arcStartAng = (firstEnd.pt - node).angle
                val arcEndAng = (secondStart.pt - node).angle
                CircleDrawing.arc(node.x, node.y, radius, arcStartAng, arcEndAng).map(interiorPoint).toList
            } else {
                val first = createJtsLine(firstStart.pt, firstEnd.pt)
                val second = createJtsLine(secondStart.pt, secondEnd.pt)
                val intersection = first.intersection(second)
                intersection match {
                    case pt: Point if !pt.isEmpty => {
                        List(interiorPoint(vector(pt.getX, pt.getY)))
                    }
                    case _ => throw new Exception("Not getting intersection")
                }
            }
        }

        def createJtsLine(p1: Position, p2: Position) = {
            val x = p1.x
            geomFact.createLineString(Array(new Coordinate(p1.x, p1.y), new Coordinate(p2.x, p2.y)))
        }

        def connectAroundEdgeBothDefined(edgeStart: Position, edgeEnd: Position)(firstStart: ResultPoint, firstEnd: ResultPoint)(secondStart: ResultPoint, secondEnd: ResultPoint): List[ResultPoint] = {
            val first = createJtsLine(firstStart.pt, firstEnd.pt)
            val second = createJtsLine(secondStart.pt, secondEnd.pt)
            val intersection = first.intersection(second)
            intersection match {
                case ls: LineString if !ls.isEmpty => Nil
                case point: Point if !point.isEmpty => {
                    List(interiorPoint(vector(point.getX, point.getY)))
                }
                case other => {
                    if (firstEnd.onEdge && secondStart.onEdge)
                        List(firstEnd, secondStart)
                    else
                        Nil
                }
            }
        }

        def connectAroundEdge(edgeStart: Position, edgeEnd: Position, first: Option[(ResultPoint, ResultPoint)], second: Option[(ResultPoint, ResultPoint)]): List[ResultPoint] = {
            (first, second) match {
                case (Some(f), Some(s)) => (connectAroundEdgeBothDefined(edgeStart, edgeEnd) _).tupled(f).tupled(s)
                case (Some((firstStart, firstEnd)), None) => List(firstEnd)
                case (None, Some((secondStart, secondEnd))) => List(secondStart)
                case _ => Nil
            }
        }

        val lines = for (Seq(a, b) <- edges) yield {
            (createLine(a, b).map((extractForward _).tupled),
                createLine(b, a).map((extractBack _).tupled))
        }

        val resultPoints = {
            val edgePoints = (lines zip edges).map {
                case ((firstEdge, secondEdge), Seq(a, b)) => connectAroundEdge(a.pos, b.pos, firstEdge, secondEdge)
            }

            val nodePoints = ((lines :+ lines.head).sliding(2) zip edges.map(_.last).iterator).map {
                case (Seq((_, Some(e1)), (Some(e2), _)), point) => (connectAroundNode(point.pos) _).tupled(e1).tupled(e2)
                case _ => Nil
            }

            (edgePoints.iterator zip nodePoints).flatMap {
                case (a, b) => Seq(a, b)
            }.flatten.toList
        }

        val linestrings = {
            val patches = resultPoints.split(_.onEdge && _.onEdge)
            val lastPatch = patches.last
            val lastPoint = lastPatch.last
            val connected = if (lastPoint.onEdge & patches.head.head.onEdge)
                patches
            else
                (lastPatch ++ patches.head) :: patches.tail.dropRight(1)

            def lsFromLst(lst: List[ResultPoint]) = {
                geomFact.createLineString(lst.map(p => new Coordinate(p.pt.x, p.pt.y)).toArray)
            }

            if (connected.size == 1)
                List(lsFromLst(connected.head :+ connected.head.head))
            else
                connected.map(lsFromLst)
        }

        val result: Option[Geometry] = if (linestrings.size == 1)
            Some(linestrings.head)
        else
            Some(geomFact.createMultiLineString(linestrings.toArray))
    }

    trait QuickAreaVisualizer extends AreaVisualizer {
        import ListPositionImplicit._

    }
}