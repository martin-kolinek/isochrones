package org.isochrone.executors

import org.isochrone.ActionExecutor
import org.isochrone.ActionComponent
import org.isochrone.dijkstra.DijkstraAlgorithmComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.dbgraph.ConfigDatabaseGraphComponent
import org.isochrone.db.SingleSessionProvider
import org.isochrone.Main
import org.isochrone.db.ConfigRoadNetTableComponent
import org.isochrone.OptionParserComponent
import org.isochrone.ArgumentParser
import scopt.OptionParser
import shapeless.Lens
import com.typesafe.scalalogging.slf4j.Logging

trait DistanceExecutor extends ActionExecutor {
    self: Main.type =>
    abstract override def actions = super.actions + ("distance" --> new ActionComponent 
            with FromOptionDatabaseComponent
            with ConfigRoadNetTableComponent 
            with ConfigDatabaseGraphComponent 
            with SingleSessionProvider 
            with OptionsBase 
            with DistanceParamsComponent
            with DijkstraAlgorithmComponent
            with Logging {
        val execute = () => {
            val dist = DijkstraHelpers.compute(distParams.start).find(_._1 == distParams.end).map(_._2).getOrElse(Double.PositiveInfinity)
            logger.info(s"Distance between ${distParams.start} and ${distParams.end} is $dist")
        }
    })

}

trait DistanceParamsComponent extends OptionParserComponent {
    self: ArgumentParser =>

    case class DistanceParams(start: Long, end: Long)

    lazy val distParamLens = registerConfig(DistanceParams(0, 0))
    private def startLens = (Lens[DistanceParams] >> 0) compose distParamLens
    private def endLens = (Lens[DistanceParams] >> 1) compose distParamLens

    lazy val distParams = distParamLens.get(parsedConfig)

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) {

        super.parserOptions(pars)
        pars.opt[Long]("start").action((x, c) => startLens.set(c)(x))
        pars.opt[Long]("end").action((x, c) => endLens.set(c)(x))
    }
}