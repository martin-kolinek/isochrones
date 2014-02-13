package org.isochrone.hh

import org.scalatest.FunSuite
import org.isochrone.graphlib.GraphComponent
import org.isochrone.simplegraph.SimpleGraphComponent

class LineContractionTest extends FunSuite {
    test("Line contraction works without db") {
        val comp = new LineContractionComponentBase with SimpleGraphComponent {
            type NodeType = Int

            val contractor = new LineContractionBase {
                val graph = SimpleGraph.undirOneCost(1 -> 2, 2 -> 3, 3 -> 4, 4 -> 5)
            }
            val ln = contractor.getNodeLine(3)
            if (ln.start == 5) {
                assert(ln.end === 1)
                assert(ln.inner === List(4, 3, 2))
            } else if (ln.start == 1) {
                assert(ln.end === 5)
                assert(ln.inner === List(2, 3, 4))
            } else
                fail()

            val shortcuts = contractor.getShortcuts(ln)
            val grouped = shortcuts.groupBy(_._2).mapValues { v =>
                v.map {
                    case (s, e, c) => s -> c
                }.toSet
            }

            assert(grouped.keySet === Set(1, 5))
            assert(grouped(1) === (2 to 5).map(x => x -> (x - 1)).toSet)
            assert(grouped(5) === (1 to 4).map(x => x -> (5 - x)).toSet)
        }

    }
}