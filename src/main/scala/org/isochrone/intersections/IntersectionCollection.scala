package org.isochrone.intersections

import org.isochrone.db.OsmTableComponent
import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory

class IntersectionCollection(input: Seq[(Long, Geometry, Long, Geometry, Long, Geometry, Long, Geometry)], maxNode: Long) {
    val nodeGeoms = (for {
        (a, ag, b, bg, c, cg, d, dg) <- input
    } yield Seq(a -> ag, b -> bg, c -> cg, d -> dg)).flatten.toMap

    type Edge = Set[Long]

    type Intersection = Set[Edge]

    def edge(a: Long, b: Long) = Set(a, b)

    def intersection(a: Long, b: Long, c: Long, d: Long) = Set(edge(a, b), edge(c, d))

    val edgeIntersections: Map[Edge, Set[Intersection]] = (for {
        (a, _, b, _, c, _, d, _) <- input
        inters = intersection(a, b, c, d)
    } yield Seq(edge(a, b) -> inters, edge(c, d) -> inters)).flatten.groupBy(_._1).map {
        case (key, values) => key -> values.map(_._2).toSet
    }

    val geomfact = new GeometryFactory

    val intersectionPoints: Map[Intersection, Point] = (for {
        (a, ag, b, bg, c, cg, d, dg) <- input
        abline = geomfact.createLineString(Array(ag.getCoordinate, bg.getCoordinate))
        cdline = geomfact.createLineString(Array(cg.getCoordinate, dg.getCoordinate))
    } yield Set(edge(a, b), edge(c, d)) -> (abline intersection cdline).getInteriorPoint).toMap

    val intersectionNodes: Map[Intersection, Long] = (for {
        (a, _, b, _, c, _, d, _) <- input
    } yield intersection(a, b, c, d)).toSet.zipWithIndex.map(x => x._1 -> (x._2.toLong + maxNode)).toMap

    def intersectionsForEdge(start: Long, end: Long): Seq[Long] = {
        val intersections = edgeIntersections(Set(start, end))
        val startPoint = nodeGeoms(start)
        val sorted = intersections.toSeq.sortBy(i => intersectionPoints(i) distance startPoint)
        sorted.map(intersectionNodes)
    }

    def newForDb: Seq[(Long, Point)] = {
        intersectionNodes.map(nd => nd._2 -> intersectionPoints(nd._1)).toSeq
    }
}
