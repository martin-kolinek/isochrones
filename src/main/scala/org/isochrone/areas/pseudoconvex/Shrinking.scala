package org.isochrone.areas.pseudoconvex

import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.OptionParserComponent
import scopt.OptionParser
import org.isochrone.ArgumentParser
import org.isochrone.areas.PosAreaComponent
import org.isochrone.areas.AreaReaderComponent
import shapeless.Lens

trait ShrinkingRatioComponent {
    def amounts: List[Double]
}

trait ShrinkRatioParserComponent extends OptionParserComponent {
    val amountLens = registerConfig(List(0.000001))
    val amountStrLens = new Lens[List[Double], String] {
        def get(l: List[Double]) = l.mkString(",")
        def set(c:List[Double])(s: String) = s.split(",").map(_.toDouble).toList
    }.compose(amountLens)
    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[String]("shrink-amount").
            text("The ratio from minimum distance between points in area used to shrink areas for polygonization (default = 0.000001)").
            action((x, c) => amountStrLens.set(c)(x))
    }
}

trait ConfigShrinkRatioComponent extends ShrinkingRatioComponent with ShrinkRatioParserComponent {
    self: ArgumentParser =>
    def amounts:List[Double] = amountLens.get(parsedConfig)
}