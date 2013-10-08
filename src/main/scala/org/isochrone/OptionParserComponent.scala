package org.isochrone

import scopt.OptionParser

trait OptionParserComponent {
    type OptionConfig

    def parser: OptionParser[OptionConfig]

    def parserStart: OptionConfig

    class CommonOptionParser extends OptionParser[OptionConfig]("") {
        help("help") text ("print this help")
    }
}