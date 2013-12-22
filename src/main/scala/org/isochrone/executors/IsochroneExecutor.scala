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
import org.isochrone.visualize.AreaCacheComponent
import org.isochrone.visualize.AreaGeometryCacheComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.SessionProviderComponent

trait IsochroneExecutorCompoent extends DefaultIsochronesComputationComponent with VisualizationIsochroneOutputComponent with ArgumentParser with GeometryOutputComponent with AreaCacheComponent with AreaGeometryCacheComponent {
    self: SomeIsochroneComputerComponent with NodePositionComponent with GraphComponentBaseWithDefault with RoadNetTableComponent with SessionProviderComponent =>

    val areaCache = ???
    val areaGeomCache = ???

    val execute = () => writeOutput()
}

