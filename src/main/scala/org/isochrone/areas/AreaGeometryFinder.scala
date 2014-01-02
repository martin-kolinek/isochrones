package org.isochrone.areas

import org.isochrone.graphlib.GraphComponentBase
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.PrecisionModel
import com.vividsolutions.jts.geom.Coordinate
import org.isochrone.util._
import com.vividsolutions.jts.geom.Geometry
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

trait AreaGeometryFinderComponent extends PosAreaComponent {
    self: GraphComponentBase =>

    import ListPositionImplicit._

    object AreaGeometryFinder {
        def filterInnerNodes(ar: PosArea) = {
            val pts = ar.points.map(_.nd)

            val edges = (ar.points.last +: ar.points).sliding(2).toList
            val revEdges = edges.map(_.reverse).toSet

            val filtered = edges.filterNot(revEdges.contains)
            val consecutive = filtered.split(_.tail.head != _.head)
            joinPatches(consecutive).map(x => PosArea(ar.id, x, ar.costs))
        }

        def joinPatches(patches: List[List[List[PointWithPosition]]]): Iterable[List[PointWithPosition]] = {
            val lasts = new HashMap[NodeType, ListBuffer[PointWithPosition]]
            for (lst <- patches) {
                val last = lst.last.last
                val first = lst.head.head
                if (!lasts.contains(first.nd))
                    lasts(first.nd) = new ListBuffer
                lasts(first.nd) ++= lst.map(_.head)
                lasts(last.nd) = lasts(first.nd)
                if (first.nd != last.nd)
                    lasts -= first.nd
            }
            lasts.map(_._2.toList)
        }

        val geomFact = new GeometryFactory(new PrecisionModel(), 4326)

        def areaGeometry(ar: PosArea): Geometry = {
            val areas = filterInnerNodes(ar)
            val polys = areas.map(x => geomFact.createPolygon(x.toLinearRing))
            val shell = polys.find { poly =>
                polys.filterNot(_ eq poly).forall(inner => poly.contains(inner))
            }
            shell match {
                case None => throw new Exception("shell not found")
                case Some(sh) => {
                    val inner = polys.filterNot(_ eq sh)
                    ((sh: Geometry) /: inner)(_ difference _)
                }
            }
        }
    }
}