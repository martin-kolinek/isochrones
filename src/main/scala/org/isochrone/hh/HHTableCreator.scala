package org.isochrone.hh

import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._

trait HHTableCreatorComponent {
    self: HHTableComponent with DatabaseProvider =>
    object HHTableCreator {
        private def ddls = (hhTables.neighbourhoods.ddl ++ hhTables.shortcutEdges.ddl)

        def createTables() = database.withSession { implicit s: Session =>
            ddls.create
        }

        def dropTables() = database.withSession { implicit s: Session =>
            ddls.drop
        }
    }
}