package org.isochrone.areas

import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.util._
import spire.syntax.normedVectorSpace._
import spire.std.seq._
import spire.std.double._
import scalaz.syntax.id._
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.PrecisionModel
import com.vividsolutions.jts.geom.Coordinate
import com.typesafe.scalalogging.slf4j.Logging

trait PosAreaComponent extends AreaComponent {
    self: GraphComponentBase =>

    type Position = List[Double]

    object ListPositionImplicit {
        implicit class PositionOps(lst: Position) {

            def angle = math.atan2(lst.y, lst.x)

            def middleVect(other: Position) = {
                val thisAng = angle
                val otherAng = other.angle
                val normOtherAng =
                    if (otherAng <= thisAng) otherAng + 2 * math.Pi
                    else otherAng
                val addition = (normOtherAng - thisAng) / 2.0
                val newAng = thisAng + addition
                List(math.cos(newAng), math.sin(newAng))
            }
        }
    }

    import ListPositionImplicit._

    case class PointWithPosition(val nd: NodeType, val pos: Position)

    private val geomFact = new GeometryFactory(new PrecisionModel(), 4326)

    private def ptToCoordinate(pt: PointWithPosition) = new Coordinate(pt.pos.x, pt.pos.y)

    case class EdgeWithCost(nds: Set[NodeType], cost: Double)

    case class PosArea(id: Long, points: List[PointWithPosition], costs: Map[(NodeType, NodeType), Double]) extends Logging {
        def cost(nd1: NodeType, nd2: NodeType) = costs((nd1, nd2))

        def shrink(rat: Double) = {
            val l1 :: l2 :: rest = points
            val corners = (points ++ List(l1, l2)).sliding(3)
            val it = for (List(l, c, r) <- corners) yield {
                val lv = (l.pos - c.pos)
                val rv = (r.pos - c.pos)
                val mid = (lv middleVect rv) :* rat
                PointWithPosition(c.nd, c.pos + mid)
            }
            PosArea(id, it.toList, costs)
        }

        lazy val edgeSet = {
            val nds = points.map(_.nd)
            (nds :+ nds.head).sliding(2).map(_.toSet).toSet
        }

        private def coords = (points :+ points.head).map(ptToCoordinate).toArray
        def toLinearRing = geomFact.createLinearRing(coords)
        def toLineString = geomFact.createLineString(coords)

        private case class BoundBox(top: Double, left: Double, bottom: Double, right: Double) {
            val height = top - bottom
            val width = right - left
            lazy val longer = {
                math.max(width, height)
            }

            lazy val center = {
                List(left + width / 2, bottom + height / 2)
            }
        }

        private lazy val bounds = {
            val top = points.map(_.pos.y).max
            val left = points.map(_.pos.x).min
            val bottom = points.map(_.pos.y).min
            val right = points.map(_.pos.x).max
            BoundBox(top, left, bottom, right)
        }

        def normalize = {
            def normPoint(pt: PointWithPosition) = {
                val pos = (pt.pos - bounds.center) :/ bounds.longer
                PointWithPosition(pt.nd, pos)
            }
            PosArea(id, points.map(normPoint), costs)
        }

        override def toString = s"${points.map(_.nd)}, ${toLineString.toString}"

        def area = Area(points.map(_.nd))
    }
}