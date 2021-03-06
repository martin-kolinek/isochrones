package org.isochrone.compute

import org.scalatest.FunSuite
import org.isochrone.ArgumentParser
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.graphlib.GraphComponent
import org.isochrone.graphlib.GraphType
import scopt.Read
import org.isochrone.graphlib.GraphComponentBaseWithDefault

class IsochroneComputationTest extends FunSuite {
    test("IsochroneComputation works") {
        val comp = new DefaultIsochronesComputationComponent with SomeIsochroneComputerComponent with ArgumentParser with GraphComponentBaseWithDefault with IsochroneParamsParsingComponent {
            type NodeType = Int
            def noNode = 0
            val readNodeType = implicitly[Read[NodeType]]
            def parsedConfig: OptionConfig = isoParamLens.set(parserStart)(IsochroneParams(0, 10))
            val isoComputer: IsochroneComputer = new IsochroneComputer {
                def isochrone(start: Traversable[(Int, Double)], max: Double) = {
                    assert(max == 10.0)
                    assert(start == Seq(0 -> 0.0))
                    List(IsochroneNode(1, 0.5))
                }
            }
        }

        assert(comp.isochrone == List(comp.IsochroneNode(1, 0.5)))
    }
}