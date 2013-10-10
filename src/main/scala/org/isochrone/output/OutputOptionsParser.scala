package org.isochrone.output

import org.isochrone.OptionParserComponentBase
import scopt.OptionParser

trait OutputOptionsParserComponent extends OptionParserComponentBase {
    trait OutputOptionConfig {
        def file: Option[String]
        def withFile(f: String): OptionConfig
    }

    type OptionConfig <: OutputOptionConfig

    trait OutputOptionsParser {
        self: OptionParser[OptionConfig] =>
        def outputOpt = {
            opt[String]('o', "output").action((x, c) => c.withFile(x))
        }
    }
}