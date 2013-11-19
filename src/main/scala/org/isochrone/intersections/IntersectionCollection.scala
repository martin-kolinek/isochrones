package org.isochrone.intersections

import org.isochrone.db.OsmTableComponent
import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.PrecisionModel
import com.vividsolutions.jts.geom.LineString

class IntersectionCollection(input: Seq[(Long, Long, LineString, Long, Long, LineString)], maxNode: Long) {

    val nodeGeoms = (for {
        (a, b, abg, c, d, cdg) <- input
    } yield Seq(a -> abg.getStartPoint,
        b -> abg.getEndPoint,
        c -> cdg.getStartPoint,
        d -> cdg.getEndPoint)).flatten.toMap

    type Edge = Set[Long]

    type Intersection = Set[Edge]

    def edge(a: Long, b: Long) = Set(a, b)

    def intersection(a: Long, b: Long, c: Long, d: Long) = Set(edge(a, b), edge(c, d))

    val edgeIntersections: Map[Edge, Set[Intersection]] = (for {
        (a, b, _, c, d, _) <- input
        inters = intersection(a, b, c, d)
    } yield Seq(edge(a, b) -> inters, edge(c, d) -> inters)).flatten.groupBy(_._1).map {
        case (key, values) => key -> values.map(_._2).toSet
    }

    val geomfact = new GeometryFactory(new PrecisionModel, 4326)

    val intersectionPoints: Map[Intersection, Point] = (for {
        (a, b, abg, c, d, cdg) <- input
        abline = geomfact.createLineString(Array(abg.getStartPoint.getCoordinate, abg.getEndPoint.getCoordinate))
        cdline = geomfact.createLineString(Array(cdg.getStartPoint.getCoordinate, cdg.getEndPoint.getCoordinate))
    } yield Set(edge(a, b), edge(c, d)) -> (abline intersection cdline).getInteriorPoint).toMap

    val intersectionNodes: Map[Intersection, Long] = (for {
        (a, b, _, c, d, _) <- input
    } yield intersection(a, b, c, d)).toSet.zipWithIndex.map(x => x._1 -> (x._2.toLong + 1 + maxNode)).toMap

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
