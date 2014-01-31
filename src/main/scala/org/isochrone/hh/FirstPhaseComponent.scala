package org.isochrone.hh

import org.isochrone.graphlib.GraphComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.db.HigherLevelRoadNetTableComponent

trait FirstPhaseComponent extends GraphComponentBase {
    self: GraphComponent with HigherLevelRoadNetTableComponent =>
    type NodeType = Long

    object FirstPhase {
        //def 
        
        def processNode(nd: NodeType) = {
        	
        }
    }
}