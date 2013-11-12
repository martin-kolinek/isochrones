package org.isochrone.areas.pseudoconvex

import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.OptionParserComponent
import scopt.OptionParser
import org.isochrone.ArgumentParser

trait AreaShrinkerComponent {
    self: GraphComponentBase with PosAreaComponent with AreaReaderComponent with ShrinkingRatioComponent =>

    object shrinkedReader {
        def areas = reader.areas.map(_.shrink(ratio))
    }

}

trait ShrinkingRatioComponent {
    def ratio: Double
}

trait ShrinkRatioParserComponent extends OptionParserComponent {
    val ratioLens = registerConfig(0.01)
    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[Double]("shrink-ratio").
            text("The ratio from minimum distance between points in area used to shrink areas for polygonization (default = 0.01)").
            action((x, c) => ratioLens.set(c)(x))
    }
}

trait ConfigShrinkRatioComponent extends ShrinkingRatioComponent with ShrinkRatioParserComponent {
    self: ArgumentParser =>
    def ratio = ratioLens.get(parsedConfig)
}