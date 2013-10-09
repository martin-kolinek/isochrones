package org.isochrone.compute

import org.isochrone.OptionParserComponent
import org.isochrone.graphlib.GraphComponentBase
import scopt.OptionParser
import scopt.Read
import org.isochrone.OptionParserComponentBase

trait IsochroneParamsParsingComponent extends OptionParserComponentBase {
    self: GraphComponentBase =>

    type NodeType

    trait IsochroneParams {
        def start: NodeType
        def withNewStart(ns: NodeType): OptionConfig
        def limit: Double
        def withNewLimit(l: Double): OptionConfig
    }

    type OptionConfig <: IsochroneParams

    trait IsochroneParamsParser {
        self: OptionParser[OptionConfig] =>

        def isoOptions(implicit ev: Read[NodeType]) = {
            opt[NodeType]('s', "start").action((x, c) => c.withNewStart(x))
            opt[Double]('l', "limit").action((x, c) => c.withNewLimit(x))
        }
    }
}