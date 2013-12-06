package org.isochrone.areas

import org.isochrone.graphlib.GraphComponentBase
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.PrecisionModel
import com.vividsolutions.jts.geom.Coordinate

trait AreaGeometryFinderComponent extends PosAreaComponent {
    self: GraphComponentBase =>

    import ListPositionImplicit._

    object AreaGeometryFinder {
        def filterInnerNodes(ar: PosArea) = {
            val pts = ar.points.map(_.nd)
            val nodeCounts = pts.groupBy(identity).map {
                case (a, b) => a -> b.size
            }

            val edges = (ar.points.last +: ar.points).sliding(2).toList
            val revEdges = edges.map(_.reverse).toSet

            val filtered = edges.filterNot(revEdges.contains)
            val ndList = filtered.map(_.head)
            PosArea(ar.id, ndList, ar.costs)
        }

        val geomFact = new GeometryFactory(new PrecisionModel(), 4326)

        def areaGeometry(ar: PosArea) = {
            geomFact.createPolygon(filterInnerNodes(ar).toLinearRing)
        }
    }
}