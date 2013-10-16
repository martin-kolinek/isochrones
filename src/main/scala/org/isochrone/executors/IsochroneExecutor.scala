package org.isochrone.executors

import org.isochrone.db.DatabaseOptionParsingComponent
import org.isochrone.output.OutputOptionsParserComponent
import org.isochrone.compute.IsochroneParamsParsingComponent
import org.isochrone.compute.IsochroneComputerComponent
import org.isochrone.compute.DefaultIsochronesComputationComponent
import org.isochrone.compute.PointIsochroneOutputComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.compute.SomeIsochroneComputerComponent
import org.isochrone.graphlib.NodePositionComponent
import org.isochrone.ArgumentParser
import org.isochrone.graphlib.GraphComponent

trait IsochroneExecutorCompoent extends DefaultIsochronesComputationComponent with PointIsochroneOutputComponent with IsochroneParamsParsingComponent with ArgumentParser {
    self: SomeIsochroneComputerComponent with GraphComponent with NodePositionComponent =>
}
