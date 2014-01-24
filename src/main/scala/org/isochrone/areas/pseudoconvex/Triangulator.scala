package org.isochrone.areas.pseudoconvex

import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.util._
import org.poly2tri.geometry.polygon.PolygonPoint
import org.poly2tri.geometry.polygon.Polygon
import scala.collection.JavaConversions._
import spire.syntax.normedVectorSpace._
import spire.std.double._
import spire.std.seq._
import org.poly2tri.Poly2Tri
import org.isochrone.areas.PosAreaComponent

trait TriangulatorComponent extends PosAreaComponent {
    self: GraphComponentBase =>
    trait Triangulator {
        def triangulate(ar: PosArea): Traversable[(NodeType, NodeType)]
    }

    val triangulator: Triangulator
}

trait Poly2TriTriangulatorComponent extends TriangulatorComponent {
    self: GraphComponentBase =>

    import ListPositionImplicit._
    type NodeType

    trait Poly2TriTriangulator extends Triangulator {
        def areaToPolygon(ar: PosArea) = {
            val pts = ar.points.map(x => new PolygonPoint(x.pos.x, x.pos.y))
            new Polygon(pts)
        }

        class AreaPosMap(ar: PosArea) {
            private val mp = ar.points.map(x => (x.pos.x, x.pos.y) -> x.nd).toMap
            def getNode(x: Double, y: Double) = {
                mp((x, y))
            }
        }

        def triangulate(ar: PosArea): Traversable[(NodeType, NodeType)] = {
            val poly = areaToPolygon(ar)
            val posMap = new AreaPosMap(ar)
            Poly2Tri.triangulate(poly)
            val tris = poly.getTriangles().filter(_.isInterior)
            val edges = tris.flatMap { tri =>
                val pts = tri.points.map(x => posMap.getNode(x.getX, x.getY))
                val edges = (pts :+ pts.head).sliding(2)
                for (edge <- edges)
                    yield edge.toSet
            }
            for (Seq(a, b) <- edges.filterNot(ar.edgeSet.contains).distinct.map(_.toSeq)) yield (a, b)
        }
    }

    val triangulator = new Poly2TriTriangulator {}
}