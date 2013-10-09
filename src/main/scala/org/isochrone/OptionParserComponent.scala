package org.isochrone

import scopt.OptionParser

trait OptionParserComponent extends OptionParserComponentBase {

    def parser: OptionParser[OptionConfig]

    def parserStart: OptionConfig

}

trait OptionParserComponentBase {
    type OptionConfig

    class CommonOptionParser extends OptionParser[OptionConfig]("") {
        help("help") text ("print this help")
    }
}