package org.isochrone.areas

import org.isochrone.graphlib.GraphWithRegionsComponent
import org.isochrone.graphlib.NodePositionComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.util.db.MyPostgresDriver.simple._
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.graphlib.GraphComponentBase

trait AreaSaverComponent extends AreaIdentifierComponent with GraphComponentBase {
    self: GraphWithRegionsComponent with NodePositionComponent with DatabaseProvider with RoadNetTableComponent =>

    type NodeType <: Long

    object AreaSaver extends Logging {
        def saveAreas() = {
            database.withTransaction { implicit s: Session =>
                roadNetTables.roadAreas.delete
                for {
                    (area, i) <- AreaIdentifier.allAreas.zipWithIndex
                    areaId = (i + 1).toLong
                    (pt, seq) <- area.nodes.zipWithIndex
                } {
                    if (areaId % 100 == 0 && seq == 0)
                        logger.info(s"Saving area $areaId")
                    roadNetTables.roadAreas.insert((areaId, pt, seq, 0.0))
                }
            }
        }
    }
}