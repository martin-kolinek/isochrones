package org.isochrone.hh

import org.scalatest.FunSuite
import org.isochrone.db.TestDatabase
import org.isochrone.dbgraph.HHDatabaseGraphComponent
import org.isochrone.db.SingleSessionProvider
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.graphlib.GraphComponent
import org.isochrone.db.DefaultRoadNetTablesWithPrefix
import org.isochrone.db.NodeTable
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.dbgraph.HHDatabaseGraph

class TestGraph extends FunSuite with TestDatabase {

    def testHHGrp(higher: Option[TableQuery[NodeTable]], higherLevelAsserts: HHDatabaseGraph => Unit) {
        val comp = new GraphComponent with HHTableComponent with HHDatabaseGraphComponent with TestDatabaseComponent with SingleSessionProvider {
            val hhTables = new DefaultHHTablesWithPrefix("hhgrp_")
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("hhgrp_")
            val graph = new SessionHHDatabaseGraph(hhTables, roadNetTables, higher, 200)
        }
        val got = comp.graph.neighbours(1).toSet
        val expected = Set(2 -> 1, 3 -> 2)
        assert(got === expected)
        assert(comp.graph.neighbourhoodSize(1) === 1)
        assert(comp.graph.neighbourhoodSize(2) === 2)
        assert(comp.graph.neighbourhoodSize(3) === 3)
        assert(comp.graph.descendLimit(1) === 5)
        assert(comp.graph.descendLimit(2) === 6)
        assert(comp.graph.descendLimit(3) === 7)
        assert(comp.graph.shortcutReverseLimit(1) === 13)
        assert(comp.graph.shortcutReverseLimit(2) === 15)
        assert(comp.graph.shortcutReverseLimit(3) === Double.PositiveInfinity)
        higherLevelAsserts(comp.graph)
    }

    test("HHDatabaseGraph works") {
        testHHGrp(Some(TableQuery(t => new NodeTable(t, "hhgrp_higher_nodes"))), { graph =>
            assert(graph.hasHigherLevel(1))
            assert(graph.hasHigherLevel(2))
            assert(!graph.hasHigherLevel(3))
        })
    }

    test("HHDatabaseGraph works without higher level") {
        testHHGrp(None, { graph =>
            assert(!graph.hasHigherLevel(1))
            assert(!graph.hasHigherLevel(2))
            assert(!graph.hasHigherLevel(3))
        })
    }
}
