package org.isochrone.compute

import org.isochrone.OptionParserComponent
import org.isochrone.graphlib.GraphComponentBase
import scopt.OptionParser
import scopt.Read
import org.isochrone.OptionParserComponent
import shapeless._
import org.isochrone.graphlib.GraphComponent
import org.isochrone.graphlib.GraphComponentBaseWithDefault

trait IsochroneParamsParsingComponent extends OptionParserComponent {
    self: GraphComponentBaseWithDefault =>

    case class IsochroneParams(start: NodeType, limit: Double)

    lazy val isoParamLens = registerConfig(IsochroneParams(noNode, 0.0))

    def startNodeLens = (Lens[IsochroneParams] >> 0) compose isoParamLens
    def limitLens = (Lens[IsochroneParams] >> 1) compose isoParamLens

    implicit def readNodeType: Read[NodeType]

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[NodeType]('s', "start").action((x, c) => startNodeLens.set(c)(x))
        pars.opt[Double]('l', "limit").action((x, c) => limitLens.set(c)(x))
    }
}
