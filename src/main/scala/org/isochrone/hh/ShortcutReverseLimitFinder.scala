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
                    lim <- descendLimits if lim.nodeId === s.start
                } yield (s.start, lim.descendLimit + s.cost)
                shortcutReverse.insert(q)
            }
        }
    }
}