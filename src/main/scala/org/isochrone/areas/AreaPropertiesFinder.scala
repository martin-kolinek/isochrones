package org.isochrone.areas

import org.isochrone.dijkstra.DijkstraProvider
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DatabaseProvider
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.util.db.MyPostgresDriver.simple._

trait AreaPropertiesFinderComponent extends AreaCoverCostComponent with AreaGeometryFinderComponent {
    self: AreaReaderComponent with DijkstraProvider with GraphComponentBase with RoadNetTableComponent with DatabaseProvider =>

    type NodeType = Long

    object AreaPropertiesSaver extends Logging {
        def saveAreaProperties() {
            database.withTransaction { implicit s: Session =>
                Query(roadNetTables.areaGeoms).delete
                for (ar <- reader.areas) {
                    logger.info(s"Processing area ${ar.id}")
                    for ((nd, cst) <- AreaCoverCostDeterminer.getCostsForArea(ar)) {
                        roadNetTables.roadAreas.filter(_.nodeId === nd).filter(_.id === ar.id).map(_.costToCover).update(cst)
                    }
                    val geom = AreaGeometryFinder.areaGeometry(ar)
                    assert(geom.isValid())
                    roadNetTables.areaGeoms.insert(ar.id -> geom)
                }
            }
        }
    }
}