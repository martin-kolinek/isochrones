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

trait IsochroneExecutorCompoent extends DefaultIsochronesComputationComponent with ConfigCirclePointsCountComponent with SomePreciseAreaVisualizer with VisualizationIsochroneOutputComponent with ArgumentParser with GeometryOutputComponent with DbAreaInfoComponent with DefaultCostAssignerComponent {
    self: SomeIsochroneComputerComponent with NodePositionComponent with GraphComponentBaseWithDefault with RoadNetTableComponent with SessionProviderComponent with GraphComponent =>

    val execute = () => writeOutput()
}

