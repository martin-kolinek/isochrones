package org.isochrone.areas

import org.scalatest.FunSuite
import org.isochrone.simplegraph.SimpleGraphComponent
import org.isochrone.dijkstra.DefaultDijkstraProvider
import org.isochrone.graphlib.NodePositionComponent
import org.isochrone.graphlib.GraphWithRegionsComponent
import com.typesafe.scalalogging.slf4j.Logging
import org.scalatest.Assertions

class AreaIdentifierTest extends FunSuite with Logging {

    val edges = {
        val oneway = Seq(
            1 -> 2,
            1 -> 5,
            2 -> 6,
            2 -> 3,
            3 -> 7,
            3 -> 4,
            4 -> 13,
            5 -> 6,
            5 -> 8,
            6 -> 7,
            6 -> 11,
            7 -> 12,
            8 -> 10,
            9 -> 10,
            10 -> 11,
            11 -> 12,
            12 -> 13)
        (oneway ++ oneway.map(_.swap)).map(x => (x._1, x._2, 1.0))
    }

    val positions = Map[Int, (Double, Double)](
        1 -> (0, 0),
        2 -> (1, 0),
        3 -> (2, 0),
        4 -> (3, 0),
        5 -> (0, -1),
        6 -> (1, -1),
        7 -> (2, -1),
        8 -> (0, -2),
        9 -> (0.5, -2),
        10 -> (0.5, -3),
        11 -> (1, -2),
        12 -> (2, -2),
        13 -> (3, -2))

    val regions = {
        val r1 = Seq(1, 2, 5, 6, 8, 9, 10, 11)
        val r2 = Seq(3, 4, 7, 12, 13)
        (r1.map(x => x -> 1) ++ r2.map(x => x -> 2)).toMap
    }

    test("Area identifier works") {
        new SimpleGraphComponent with DefaultDijkstraProvider with NodePositionComponent with GraphWithRegionsComponent with AreaIdentifierComponent {
            type NodeType = Int
            val graph = SimpleGraph(edges, regions, positions)
            val nodePos = graph
            val expectedAreas = Set(
                Area(List(1, 2, 6, 5)),
                Area(List(2, 3, 7, 6)),
                Area(List(3, 4, 13, 12, 7)),
                Area(List(5, 6, 11, 10, 9, 10, 8)),
                Area(List(6, 7, 12, 11)),
                Area(List(8, 10, 11, 12, 13, 4, 3, 2, 1, 5)))

            val areas = AreaIdentifier.allAreas
            val set = areas.toSet
            assert(set.size == areas.size)
            info(set.toString)
            info(expectedAreas.toString)
            assert(expectedAreas == areas.toSet)
        }
    }

    test("Areas are computed lazily for different regions") {
        new SimpleGraphComponent with DefaultDijkstraProvider with NodePositionComponent with GraphWithRegionsComponent with AreaIdentifierComponent {
            type NodeType = Int
            var init = true
            val graph = new SimpleGraph(edges, regions, positions) {
                override def singleRegion(rg: RegionType) = if (rg == regions.head || init)
                    super.singleRegion(rg)
                else Assertions.fail
            }
            val nodePos = graph
            graph.nodeEccentrities
            init = false
            info(AreaIdentifier.allAreas.head.toString)
        }
    }
}