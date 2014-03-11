package org.isochrone.executors

import org.isochrone.output.OutputOptionsParserComponent
import org.isochrone.compute.IsochroneParamsParsingComponent
import org.isochrone.compute.IsochroneComputerComponent
import org.isochrone.compute.DefaultIsochronesComputationComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.compute.SomeIsochroneComputerComponent
import org.isochrone.graphlib.NodePositionComponent
import org.isochrone.ArgumentParser
import org.isochrone.graphlib.GraphComponent
import org.isochrone.output.GeometryOutputComponent
import org.isochrone.graphlib.GraphComponentBaseWithDefault
import org.isochrone.visualize.VisualizationIsochroneOutputComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.SessionProviderComponent
import org.isochrone.osm.DefaultCostAssignerComponent
import org.isochrone.visualize.ConfigCirclePointsCountComponent
import org.isochrone.visualize.SomePreciseAreaVisualizer
import org.isochrone.visualize.DbAreaInfoComponent
import org.isochrone.visualize.ApproxEquidistAzimuthProjComponent
import org.isochrone.visualize.SomeQuickAreaVisualizerComponent
import org.isochrone.compute.IsochroneOutputComponent
import org.isochrone.compute.PointIsochroneOutput
import org.isochrone.visualize.AreaVisualizerComponent
import org.isochrone.compute.IsochronesComputationComponent
import org.isochrone.visualize.AreaInfoComponent
import org.isochrone.OptionParserComponent
import scopt.OptionParser
import org.isochrone.compute.PointIsochroneOutput

trait IsochroneExecutorCompoent extends DefaultIsochronesComputationComponent with ConfigCirclePointsCountComponent with SomePreciseAreaVisualizer with ConfigIsochroneOutputComponent with ArgumentParser with GeometryOutputComponent with DbAreaInfoComponent with DefaultCostAssignerComponent with ApproxEquidistAzimuthProjComponent {
    self: SomeIsochroneComputerComponent with NodePositionComponent with GraphComponentBaseWithDefault with RoadNetTableComponent with SessionProviderComponent with GraphComponent =>

    val execute = () => writeOutput()
}

trait ConfigIsochroneOutputComponent extends IsochroneOutputComponent with PointIsochroneOutput with VisualizationIsochroneOutputComponent with OptionParserComponent {
    self: AreaVisualizerComponent with IsochronesComputationComponent with NodePositionComponent with AreaInfoComponent with GraphComponentBase with ArgumentParser =>

    override def isochroneGeometry = {
        if (onlyNodesLens.get(parsedConfig))
            super[PointIsochroneOutput].isochroneGeometry
        else
            super[VisualizationIsochroneOutputComponent].isochroneGeometry
    }

    lazy val onlyNodesLens = registerConfig(false)

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[Boolean]("only-nodes").action((x, c) => onlyNodesLens.set(c)(x))
    }

}