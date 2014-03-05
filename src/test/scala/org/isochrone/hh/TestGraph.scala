package org.isochrone.hh

import org.scalatest.FunSuite
import org.isochrone.db.TestDatabase
import org.isochrone.dbgraph.HHDatabaseGraphComponent
import org.isochrone.db.SingleSessionProvider
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.graphlib.GraphComponent
import org.isochrone.db.DefaultRoadNetTablesWithPrefix

class TestGraph extends FunSuite with TestDatabase {
    test("HHDatabaseGraph works") {
        val comp = new GraphComponent with HHTableComponent with HHDatabaseGraphComponent with TestDatabaseComponent with SingleSessionProvider {
            val hhTables = new DefaultHHTablesWithPrefix("hhgrp_")
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("hhgrp_")
            val graph = new SessionHHDatabaseGraph(hhTables, roadNetTables, 200)
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
        assert(comp.graph.descendLimit(1) === 13)
        assert(comp.graph.descendLimit(2) === 15)
        assert(comp.graph.descendLimit(3) === Double.PositiveInfinity)
    }
}
