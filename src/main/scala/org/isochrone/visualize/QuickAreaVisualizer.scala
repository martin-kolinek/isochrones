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
        val edges = closedArea.sliding(2)

        case class ResultPoint(pt: Position, onEdge: Boolean)

        def interiorPoint(p: Position) = ResultPoint(p, false)

        def createLine(start: PointWithPosition, end: PointWithPosition) = {
            val cost = area.cost(start.nd, end.nd)
            val center = (start.pos + end.pos) :/ 2.0

            def tangentPoints(pt: Position, remaining: Double) = {
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
                    val startPoints = tangentPoints(start.pos, remaining)
                    Some(startPoints, Seq(endPoint, endPoint))
                }
                case (Some(startRem), Some(endRem)) => {
                    val startPoints = tangentPoints(start.pos, startRem)
                    val endPoints = tangentPoints(end.pos, endRem).reverse
                    Some(startPoints, endPoints)
                }
            }
        }

        def extractForward(starts: Seq[ResultPoint], ends: Seq[ResultPoint]) = (starts.tail.head, ends.tail.head)
        def extractBack(starts: Seq[ResultPoint], ends: Seq[ResultPoint]) = (ends.head, starts.head)

        def connectAroundNode(node: Position)(firstStart: Position, firstEnd: Position)(secondStart: Position, secondEnd: Position): List[Position] = {
            val firstAng = (firstStart - firstEnd).angle
            val secondAng = (secondEnd - secondStart).angle
            val diff = normalizeAngle(secondAng - firstAng)
            if (diff > math.Pi) {
                val radius = (firstEnd - node).norm
                val arcStartAng = (firstEnd - node).angle
                val arcEndAng = (secondStart - node).angle
                CircleDrawing.arc(node.x, node.y, radius, arcStartAng, arcEndAng).toList
            } else {
                val first = createLine(firstStart, firstEnd)
                val second = createLine(secondStart, secondEnd)
                val intersection = first.intersection(second)
                intersection match {
                    case pt: Point if !pt.isEmpty => {
                        List(vector(pt.getX, pt.getY))
                    }
                    case _ => throw new Exception("Not getting intersection")
                }
            }
        }

        private def createLine(p1: Position, p2: Position) = {
            val x = p1.x
            geomFact.createLineString(Array(new Coordinate(p1.x, p1.y), new Coordinate(p2.x, p2.y)))
        }

        def connectAroundEdge(edgeStart: Position, edgeEnd: Position)(firstStart: ResultPoint, firstEnd: ResultPoint)(secondStart: ResultPoint, secondEnd: ResultPoint): List[ResultPoint] = {
            val first = createLine(firstStart.pt, firstEnd.pt)
            val second = createLine(secondStart.pt, secondEnd.pt)
            val intersection = first.intersection(second)
            intersection match {
                case ls: LineString if !ls.isEmpty => Nil
                case point: Point if !point.isEmpty => List(interiorPoint(vector(point.getX, point.getY)))
                case other => {
                    if (firstEnd.onEdge && secondStart.onEdge)
                        List(firstEnd, secondStart)
                    else
                        Nil
                }
            }
        }

        val lines = for (Seq(a, b) <- edges) yield {
            List(createLine(a, b).map((extractForward _).tupled),
                createLine(b, a).map((extractBack _).tupled))
        }

        //val result: Option[Geometry] = ???
    }

    trait QuickAreaVisualizer extends AreaVisualizer {
        import ListPositionImplicit._

    }
}