package org.isochrone.intersections

import org.scalatest.FunSuite
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Coordinate

class IntersectionCollectionTest extends FunSuite {
    test("IntersectionCollection works") {
        val fact = new GeometryFactory()
        val zero = fact.createPoint(new Coordinate(0, 0))
        val xone = fact.createPoint(new Coordinate(1, 0))
        val yone = fact.createPoint(new Coordinate(0, 1))
        val one = fact.createPoint(new Coordinate(1, 1))
        val svndwn = fact.createPoint(new Coordinate(0.7, 0))
        val svnup = fact.createPoint(new Coordinate(0.7, 1))
        val center = fact.createPoint(new Coordinate(0.5, 0.5))
        val svn = fact.createPoint(new Coordinate(0.7, 0.7))
        val input = Seq(
            (1l, zero, 2l, one, 3l, xone, 4l, yone),
            (1l, zero, 2l, one, 4l, yone, 3l, xone),
            (2l, one, 1l, zero, 3l, xone, 4l, yone),
            (2l, one, 1l, zero, 4l, yone, 3l, xone),
            (3l, xone, 4l, yone, 1l, zero, 2l, one),
            (4l, yone, 3l, xone, 1l, zero, 2l, one),
            (3l, xone, 4l, yone, 2l, one, 1l, zero),
            (4l, yone, 3l, xone, 2l, one, 1l, zero),
            (1l, zero, 2l, one, 5l, svnup, 6l, svndwn),
            (1l, zero, 2l, one, 6l, svndwn, 5l, svnup),
            (2l, one, 1l, zero, 5l, svnup, 6l, svndwn),
            (2l, one, 1l, zero, 6l, svndwn, 5l, svnup),
            (5l, svnup, 6l, svndwn, 1l, zero, 2l, one),
            (6l, svndwn, 5l, svnup, 1l, zero, 2l, one),
            (5l, svnup, 6l, svndwn, 2l, one, 1l, zero),
            (6l, svndwn, 5l, svnup, 2l, one, 1l, zero))
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