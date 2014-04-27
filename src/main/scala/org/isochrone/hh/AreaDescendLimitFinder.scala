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
                val l0 = roadNetTables.roadAreas.groupBy(_.nodeId).map {
                    case (nid, lst) => nid -> lst.map(_.costToCover).max.ifNull(0.0)
                }
                
                val maxAreaL0 = (for {
                    l <- l0
                    a <- roadNetTables.roadAreas if a.nodeId === l._1
                } yield (a.id, l._2)).groupBy(_._1).map {
                    case (id, rows) => id -> rows.map(_._2).max
                }
                
                val insQ = (for {
                    l <- maxAreaL0
                    a <- roadNetTables.roadAreas if a.id === l._1
                } yield (a.nodeId, l._2)).groupBy(_._1).map {
                    case (nodeId, rows) => nodeId -> rows.map(_._2).max.ifNull(0.0)
                }
                
                hhTables.descendLimit.insert(insQ)
            }
        }
    }
}