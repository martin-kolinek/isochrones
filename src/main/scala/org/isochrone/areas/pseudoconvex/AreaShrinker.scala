package org.isochrone.areas.pseudoconvex

import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.OptionParserComponent
import scopt.OptionParser
import org.isochrone.ArgumentParser

trait AreaShrinkerComponent extends AreaReaderComponent with PosAreaComponent {
    self: GraphComponentBase with ShrinkingRatioComponent =>

    trait ShrinkedReader extends AreaReader {
        abstract override def areas = super.areas.map(_.shrink(amount))
    }
}

trait ShrinkingRatioComponent {
    def amount: Double
}

trait ShrinkRatioParserComponent extends OptionParserComponent {
    val amountLens = registerConfig(0.000001)
    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[Double]("shrink-amount").
            text("The ratio from minimum distance between points in area used to shrink areas for polygonization (default = 0.000001)").
            action((x, c) => amountLens.set(c)(x))
    }
}

trait ConfigShrinkRatioComponent extends ShrinkingRatioComponent with ShrinkRatioParserComponent {
    self: ArgumentParser =>
    def amount = amountLens.get(parsedConfig)
}