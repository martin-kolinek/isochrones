package org.isochrone.visualize

import org.isochrone.ArgumentParser
import org.isochrone.OptionParserComponent
import scopt.OptionParser

trait CirclePointsCountComponent {
    def circlePointCount: Int
}

trait ConfigCirclePointsCountComponent extends CirclePointsCountComponent with VisualizationConfigComponent {
    self: ArgumentParser =>

    lazy val circlePointCount = circlePointsLens.get(parsedConfig)
}

trait VisualizationConfigComponent extends OptionParserComponent {
    val circlePointsLens = registerConfig(40)

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[Int]("circle-points").text("Number of points in circles (default = 40)").action((x, c) => circlePointsLens.set(c)(x))
    }
}