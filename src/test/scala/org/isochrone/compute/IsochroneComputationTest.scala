package org.isochrone.compute

import org.scalatest.FunSuite
import org.isochrone.ArgumentParser
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.graphlib.GraphComponent
import org.isochrone.graphlib.GraphType

class IsochroneComputationTest extends FunSuite {
    test("IsochroneComputation works") {
        val comp = new DefaultIsochronesComputationComponent with SomeIsochroneComputerComponent with ArgumentParser with GraphComponent with IsochroneParamsParsingComponent {
            type NodeType = Int
            val graph = new GraphType[Int] {
                def nodes = List(1)
                def neighbours(nd: Int) = Nil
            }
            def parsedConfig: OptionConfig = isoParamLens.set(parserStart)(IsochroneParams(0, 10))
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