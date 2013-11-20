package org.isochrone.areas.pseudoconvex

import org.isochrone.graphlib.GraphComponentBase
import spire.algebra.NormedVectorSpace
import spire.algebra.MetricSpace
import spire.syntax.normedVectorSpace._
import spire.std.seq._
import spire.std.double._
import scalaz.syntax.id._
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.PrecisionModel
import com.vividsolutions.jts.geom.Coordinate
import com.typesafe.scalalogging.slf4j.Logging

trait PosAreaComponent {
    self: GraphComponentBase =>

    type Position = List[Double]

    object ListPositionImplicit {
        implicit class PositionOps(lst: Position) {
            def x = lst.head
            def y = lst.tail.head

            def middleVect(other: Position) = {
                val thisAng = math.atan2(y, x)
                val otherAng = math.atan2(other.y, other.x)
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

    case class Area(points: List[PointWithPosition], costs: Map[(NodeType, NodeType), Double]) extends Logging {
        def cost(nd1: NodeType, nd2: NodeType) = costs((nd1, nd2))

        lazy val minDist = {
            logger.debug(s"minDist of area of size: ${points.size}")
            val pairs = for {
                PointWithPosition(n1, p1) <- points
                PointWithPosition(n2, p2) <- points if n1 != n2
            } yield (p1 - p2).norm
            pairs.min
        }
        def shrink(rat: Double) = {
            val l1 :: l2 :: rest = points
            val corners = (points ++ List(l1, l2)).sliding(3)
            val it = for (List(l, c, r) <- corners) yield {
                val dist = minDist * rat
                val lv = (l.pos - c.pos)
                val rv = (r.pos - c.pos)
                val mid = (lv middleVect rv) :* dist
                PointWithPosition(c.nd, c.pos + mid)
            }
            Area(it.toList, costs)
        }

        lazy val edgeSet = {
            val nds = points.map(_.nd)
            (nds :+ nds.head).sliding(2).map(_.toSet).toSet
        }

        private def coords = (points :+ points.head).map(ptToCoordinate).toArray
        def toLinearRing = geomFact.createLinearRing(coords)
        def toLineString = geomFact.createLineString(coords)

        private case class BoundBox(top: Double, left: Double, bottom: Double, right: Double) {
            val width = top - bottom
            val height = right - left
            lazy val longer = {
                math.max(width, height)
            }

            lazy val center = {
                List(top - height / 2, left + width / 2)
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

            Area(points.map(normPoint), costs)
        }

        override def toString = s"${points.map(_.nd)}, ${toLineString.toString}"
    }
}