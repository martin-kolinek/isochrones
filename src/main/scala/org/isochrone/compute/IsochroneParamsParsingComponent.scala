package org.isochrone.compute

import org.isochrone.OptionParserComponent
import org.isochrone.graphlib.GraphComponentBase
import scopt.OptionParser
import scopt.Read
import org.isochrone.OptionParserComponent
import shapeless._
import org.isochrone.graphlib.GraphComponent

trait IsochroneParamsParsingComponent extends OptionParserComponent {
    self: GraphComponentBase =>

    case class IsochroneParams(start: NodeType, limit: Double)

    val isoParamLens = registerConfig[IsochroneParams](null)

    def startNodeLens = (Lens[IsochroneParams] >> 0) compose isoParamLens
    def limitLens = (Lens[IsochroneParams] >> 1) compose isoParamLens

    trait IsochroneParamsParser {
        self: OptionParser[OptionConfig] =>

        def isoOptions(implicit ev: Read[NodeType]) = {
            opt[NodeType]('s', "start").action((x, c) => startNodeLens.set(c)(x))
            opt[Double]('l', "limit").action((x, c) => limitLens.set(c)(x))
        }
    }
}