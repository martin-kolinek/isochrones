package org.isochrone.dboutput.partition

import org.isochrone.graphlib._
import resource._
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.util._
import org.isochrone.db.DatabaseProvider
import org.isochrone.db.RoadNetTables
import org.isochrone.db.RoadNetTables
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.HigherLevelRoadNetTableComponent
import org.isochrone.partition.RegionAnalyzerProviderComponent
import com.typesafe.scalalogging.slf4j.Logging
import com.vividsolutions.jts.geom.Geometry

trait HigherLevelGraphCreatorComponent extends GraphWithRegionsComponentBase {
    self: RoadNetTableComponent with HigherLevelRoadNetTableComponent with GraphWithRegionsComponent with DatabaseProvider with RegionAnalyzerProviderComponent =>

    type NodeType = Long
    type RegionType = Int

    object HigherLevelGraph extends Logging {
        def createHigherLevelGraph() {
            database.withTransaction { implicit s: Session =>
                Query(roadNetTables.roadRegions).delete
                val difRegs = for {
                    n1 <- roadNetTables.roadNodes
                    n2 <- roadNetTables.roadNodes if n1.region =!= n2.region
                    e <- roadNetTables.roadNet if e.start === n1.id && e.end === n2.id
                } yield (n1, n2, e)
                logger.info("Adding cross region edges")
                higherRoadNetTables.roadNet.insert(difRegs.map(_._3))
                for (it <- managed(difRegs.sortBy(_._1.region).map(x => x._1.region -> x._1.id).elements)) {
                    val regions = it.partitionBy(_._1)
                    for (regionNodes <- regions) {
                        val reg = regionNodes.head._1
                        logger.info(s"Processing region $reg")
                        processRegion(reg, regionNodes.map(_._2), s)
                    }
                }
            }
        }

        def processRegion(region: Int, nodes: Seq[Long], session: Session) {
            val analyzerComp = regionAnalyzerProvider.getAnalyzer(graph.singleRegion(region))
            var diam = 0.0
            for ((node, others) <- analyzerComp.RegionAnalyzer.borderNodeDistances(nodes.toSet)) {
                higherRoadNetTables.roadNodes.insert(for {
                    nd <- roadNetTables.roadNodes if nd.id === node
                } yield (nd.id, 0, nd.geom))(session)
                for ((other, dist) <- others) {
                    val q = for {
                        sn <- roadNetTables.roadNodes if sn.id === node
                        en <- roadNetTables.roadNodes if en.id === other
                    } yield (sn.id, en.id, dist, false, sn.geom.shortestLine(en.geom).asColumnOf[Geometry])
                    higherRoadNetTables.roadNet.insert(q)(session)
                }
                if (!others.isEmpty)
                    diam = math.max(diam, others.map(_._2).max)
            }
            roadNetTables.roadRegions.insert(region, diam)(session)
        }
    }
}
