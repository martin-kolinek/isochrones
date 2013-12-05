package org.isochrone.executors

import org.isochrone.ActionExecutor
import org.isochrone.Main
import org.isochrone.areas.pseudoconvex.AreaFixerComponent
import org.isochrone.ActionComponent
import org.isochrone.areas.pseudoconvex.Poly2TriTriangulatorComponent
import org.isochrone.areas.pseudoconvex.HertelMehlhortModConvexizerComponent
import org.isochrone.db.ConfigRoadNetTableComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.areas.pseudoconvex.DbEdgeCostResolverComponent
import org.isochrone.areas.DbAreaReaderComponent
import org.isochrone.osm.DefaultCostAssignerComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.areas.pseudoconvex.AreaShrinkerComponent
import org.isochrone.dijkstra.DefaultDijkstraProvider
import org.isochrone.db.SingleSessionProvider
import org.isochrone.OptionParserComponent
import org.isochrone.areas.pseudoconvex.ConfigShrinkRatioComponent
import org.isochrone.areas.pseudoconvex.AreaFixerReaderComponent
import org.isochrone.areas.pseudoconvex.DbAllCostsForAreaComponent

trait AreaFixerExecutor extends ActionExecutor {
    self: Main.type =>
        
    abstract override def actions = super.actions + ("areafix" --> new ActionComponent
        with OptionsBase
        with ConfigShrinkRatioComponent
        with AreaFixerReaderComponent
		with AreaFixerComponent 
		with Poly2TriTriangulatorComponent
		with HertelMehlhortModConvexizerComponent
		with DbAllCostsForAreaComponent
		with ConfigRoadNetTableComponent
		with GraphComponentBase
		with DbEdgeCostResolverComponent
		with DbAreaReaderComponent
		with DefaultCostAssignerComponent
		with FromOptionDatabaseComponent
		with AreaShrinkerComponent
		with DefaultDijkstraProvider
		with SingleSessionProvider
		with OptionParserComponent {
        override type NodeType = Long
    	val execute = () => AreaFixer.fixAreas()
    })
}
