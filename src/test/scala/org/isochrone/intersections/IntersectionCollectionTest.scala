package org.isochrone.intersections

import org.scalatest.FunSuite
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Coordinate

class IntersectionCollectionTest extends FunSuite {
    test("IntersectionCollection works") {
        val fact = new GeometryFactory()
        val zero = new Coordinate(0, 0)
        val xone = new Coordinate(1, 0)
        val yone = new Coordinate(0, 1)
        val one = new Coordinate(1, 1)
        val svndwn = new Coordinate(0.7, 0)
        val svnup = new Coordinate(0.7, 1)
        val center = fact.createPoint(new Coordinate(0.5, 0.5))
        val svn = fact.createPoint(new Coordinate(0.7, 0.7))
        def line(c1: Coordinate, c2: Coordinate) = fact.createLineString(Array(c1, c2))
        val input = Seq(
            (1l, 2l, line(zero, one), 3l, 4l, line(xone, yone)),
            (1l, 2l, line(zero, one), 4l, 3l, line(yone, xone)),
            (2l, 1l, line(one, zero), 3l, 4l, line(xone, yone)),
            (2l, 1l, line(one, zero), 4l, 3l, line(yone, xone)),
            (3l, 4l, line(xone, yone), 1l, 2l, line(zero, one)),
            (4l, 3l, line(yone, xone), 1l, 2l, line(zero, one)),
            (3l, 4l, line(xone, yone), 2l, 1l, line(one, zero)),
            (4l, 3l, line(yone, xone), 2l, 1l, line(one, zero)),
            (1l, 2l, line(zero, one), 5l, 6l, line(svnup, svndwn)),
            (1l, 2l, line(zero, one), 6l, 5l, line(svndwn, svnup)),
            (2l, 1l, line(one, zero), 5l, 6l, line(svnup, svndwn)),
            (2l, 1l, line(one, zero), 6l, 5l, line(svndwn, svnup)),
            (5l, 6l, line(svnup, svndwn), 1l, 2l, line(zero, one)),
            (6l, 5l, line(svndwn, svnup), 1l, 2l, line(zero, one)),
            (5l, 6l, line(svnup, svndwn), 2l, 1l, line(one, zero)),
            (6l, 5l, line(svndwn, svnup), 2l, 1l, line(one, zero)))
        val col = new IntersectionCollection(input, 6)
        assert(col.newForDb.size == 2)
        assert(col.newForDb.map(_._1).toSet == Set(7, 8))
        val centerNode = col.newForDb.find(x => (x._2 distance center) < 0.005) match {
            case None => fail()
            case Some((nd, _)) => nd
        }
        val svnNode = col.newForDb.find(x => (x._2 distance svn) < 0.005) match {
            case None => fail()
            case Some((nd, _)) => nd
        }
        assert(col.intersectionsForEdge(1, 2) == Seq(centerNode, svnNode))
        assert(col.intersectionsForEdge(2, 1) == Seq(svnNode, centerNode))
    }
}