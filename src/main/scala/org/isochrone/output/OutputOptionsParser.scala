package org.isochrone.output

import scopt.OptionParser
import org.isochrone.OptionParserComponent
import scopt.OptionParser

trait OutputOptionsParserComponent extends OptionParserComponent {
    case class OutputOptionConfig(file: Option[String])

    lazy val fileLens = registerConfig(OutputOptionConfig(None))

    val fileNameLens = fileLens >> 0

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) {
        super.parserOptions(pars)
        pars.opt[String]('o', "output").action((x, c) => fileNameLens.set(c)(Some(x)))
    }
}