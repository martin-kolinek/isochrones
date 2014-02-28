package org.isochrone.hh

import org.isochrone.db.RoadNetTables
import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.RoadNetTableComponent

trait AreaDescendLimitFinderComponent {
    self: DatabaseProvider with RoadNetTableComponent with HHTableComponent =>

    object AreaDescendLimitFinder {
        def execute() {
            database.withTransaction { implicit s: Session =>
                val insQ = roadNetTables.roadAreas.groupBy(_.nodeId).map {
                    case (nid, lst) => nid -> lst.map(_.costToCover).max.ifNull(0.0)
                }
                hhTables.descendLimit.insert(insQ)
            }
        }
    }
}