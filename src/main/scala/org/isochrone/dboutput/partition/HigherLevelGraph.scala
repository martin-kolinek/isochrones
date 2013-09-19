package org.isochrone.dboutput.partition

import org.isochrone.graphlib._
import resource._
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.util._
import org.isochrone.db.DatabaseProvider
import org.isochrone.db.MultiLevelRoadNetTableComponent
import org.isochrone.db.RoadNetTables
import org.isochrone.db.RoadNetTables
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.HigherLevelRoadNetTableComponent
import org.isochrone.partition.RegionAnalyzerProviderComponent
import org.isochrone.dbgraph.DatabaseGraphComponent
//import org.isochrone.partition.RegionAnalyzer
trait HigherLevelGraphCreatorComponent {
    self: RoadNetTableComponent with HigherLevelRoadNetTableComponent with DatabaseGraphComponent with DatabaseProvider with RegionAnalyzerProviderComponent =>
        
    object HigherLevelGraph {
        def createHigherLevelGraph(notification: Int => Unit = x => Unit) {
            database.withSession { implicit s: Session =>
                val difRegs = for {
                    n1 <- roadNetTables.roadNodes
                    n2 <- roadNetTables.roadNodes if n1.region =!= n2.region
                    e <- roadNetTables.roadNet if e.start === n1.id && e.end === n2.id
                } yield (n1, n2, e)
                higherRoadNetTables.roadNet.insert(difRegs.map(_._3))
                for (it <- managed(difRegs.sortBy(_._1.region).map(x => x._1.region -> x._1.id).elements)) {
                    val regions = it.partitionBy(_._1)
                    for (regionNodes <- regions) {
                        val reg = regionNodes.head._1
                        notification(reg)
                        processRegion(reg, regionNodes.map(_._2), s)
                    }
                }
            }
        }

        def processRegion(region: Int, nodes: Seq[Long], session:Session) {
            val analyzerComp = regionAnalyzerProvider.getAnalyzer(graph.singleRegion(DatabaseRegion(region)))
            var diam = 0.0
            for ((node, others) <- analyzerComp.RegionAnalyzer.borderNodeDistances(nodes.toSet)) {
                higherRoadNetTables.roadNodes.insert(node -> 0)(session)
                for ((other, dist) <- others) {
                    higherRoadNetTables.roadNet.insert(node, other, dist)(session)
                }
                if (!others.isEmpty)
                    diam = math.max(diam, others.map(_._2).max)
            }
            roadNetTables.roadRegions.insert(region, diam)(session)
        }
    }
}
