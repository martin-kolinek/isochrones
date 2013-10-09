package org.isochrone.compute

import org.scalatest.FunSuite
import org.isochrone.ArgumentParser
import org.isochrone.graphlib.GraphComponentBase

class IsochroneComputationTest extends FunSuite {
    test("IsochroneComputation") {
        val comp = new DefaultIsochronesComputationComponent with IsochroneParamsParsingComponent with SomeIsochroneComputerComponent with ArgumentParser with GraphComponentBase {
            case class TestIsochroneParams(start: Int, limit: Double) extends IsochroneParams {
                def withNewLimit(l: Double) = copy(limit = l)
                def withNewStart(ns: Int) = copy(start = ns)
            }
            type OptionConfig = TestIsochroneParams
            type NodeType = Int
            def parsedConfig: OptionConfig = TestIsochroneParams(0, 10)
            val isoComputer: IsochroneComputer = new IsochroneComputer {
                def isochrone(start: Traversable[(Int, Double)], max: Double) = {
                    assert(max == 10.0)
                    assert(start == Seq(0 -> 0.0))
                    List(IsochroneEdge(1, 2, 0.5))
                }
            }
        }

        assert(comp.isochrone == List(comp.IsochroneEdge(1, 2, 0.5)))
    }
}