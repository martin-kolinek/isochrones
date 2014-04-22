package org.isochrone.hh

import org.isochrone.db.EdgeTable
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.DatabaseProvider

trait ShortcutReverseLimitFinderComponent {
    self: DatabaseProvider =>
    object ShorctutReverseLimitFinder {
        def findShortcutReverseLimits(shortcutTable: TableQuery[EdgeTable], descendLimits: TableQuery[DescendLimits], shortcutReverse: TableQuery[DescendLimits]) = {
            database.withTransaction { implicit s: Session =>
                val q = for {
                    s <- shortcutTable
                    lim <- descendLimits if lim.nodeId === s.end
                } yield (s.start, lim.descendLimit + s.cost)
                val q2 = q.groupBy(_._1).map {
                    case (key, lst) => (key, lst.map(_._2).max.ifNull(100000.0))
                }
                shortcutReverse.insert(q2)
            }
        }
    }
}