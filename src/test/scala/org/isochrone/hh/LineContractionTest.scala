package org.isochrone.hh

import org.scalatest.FunSuite
import org.isochrone.graphlib.GraphComponent
import org.isochrone.simplegraph.SimpleGraphComponent

class LineContractionTest extends FunSuite {
    test("Line contraction works without db") {
        val comp = new LineContractionComponentBase with SimpleGraphComponent {
            type NodeType = Int

            val contractor = new LineContractionBase {
                val graph = SimpleGraph(
                    (1, 2, 0.5), (2, 1, 1.0),
                    (2, 3, 1.5), (3, 2, 2.0),
                    (3, 4, 2.5), (4, 3, 3.0))
            }
            val ln = contractor.getNodeLine(3).get
            if (ln.start == 4) {
                assert(ln.end === 1)
                assert(ln.inner === List(3, 2))
            } else if (ln.start == 1) {
                assert(ln.end === 4)
                assert(ln.inner === List(2, 3))
            } else
                fail()

            val contractor.ShortcutResult(shortcuts, revShort, endToEnd) = contractor.getShortcuts(ln)

            assert(shortcuts.toSet === Set((2, 1, 1.0), (3, 1, 3.0), (3, 4, 2.5), (2, 4, 4.0)))
            assert(revShort.toSet === Set((1, 2, 0.5), (1, 3, 2.0), (4, 2, 5.0), (4, 3, 3.0)))
            assert(endToEnd.toSet === Set((1, 4, 4.5), (4, 1, 6.0)))
        }
    }

    test("Line contraction ignores cycles") {
        val comp = new LineContractionComponentBase with SimpleGraphComponent {
            type NodeType = Int
            val contractor = new LineContractionBase {
                val graph = SimpleGraph.undirOneCost(1 -> 2, 2 -> 3, 3 -> 1)
            }
            assert(contractor.getNodeLine(1).isEmpty)
        }
    }
}