package org.isochrone.visualize

import org.isochrone.util._
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.PrecisionModel
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.LineString

trait CircleDrawingComponent {
    self: CirclePointsCountComponent with AzimuthalProjectionComponent =>

    object CircleDrawing {
        private val geomFact = new GeometryFactory(new PrecisionModel, 4326)

        def arcGeom(cx: Double, cy: Double, radius: Double, minAng: Double, maxAng: Double) = {
            val coords = arc(cx, cy, radius, minAng, maxAng).map { p =>
                new Coordinate(p.x, p.y)
            }
            if (coords.size == 1) {
                geomFact.createPoint(coords.head)
            } else {
                geomFact.createLineString(coords.toArray)
            }
        }

        def arc(cx: Double, cy: Double, radius: Double, minAng: Double, maxAng: Double) = {
            val proj = projectionForPoint(cx, cy)
            def pointForAngle(ang: Double) = {
                vector.tupled(proj.unproject(math.cos(ang) * radius, math.sin(ang) * radius))
            }
            if (minAng == maxAng)
                List(pointForAngle(minAng))
            else {
                val angFrac = 2 * math.Pi / circlePointCount
                val normMax = if (maxAng < minAng) maxAng + 2 * math.Pi else maxAng
                val points = for (ang <- (0 until circlePointCount).map(_ * angFrac + minAng) if ang < normMax)
                    yield pointForAngle(ang)
                val lastPoint = if (normalizeAngle(minAng) == normalizeAngle(maxAng))
                    points.head
                else
                    pointForAngle(maxAng)
                (points :+ lastPoint)
            }
        }

        def circle(cx: Double, cy: Double, radius: Double) = {
            arcGeom(cx, cy, radius, 0, 2 * math.Pi) match {
                case ls: LineString => {
                    assert(ls.isClosed)
                    geomFact.createPolygon(ls.getCoordinates())
                }
                case _ => throw new Exception("Didn't get line string from arcGeom")
            }
        }

    }

}