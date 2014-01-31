package org.isochrone.hh

import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._

trait HHTableCreatorComponent {
    self: HHTableComponent with DatabaseProvider =>
    object HHTableCreator {
        def createTables = database.withSession { implicit s: Session =>
            hhTables.neighbourhoods.ddl.create
        }

        def dropTables = database.withSession { implicit s: Session =>
            hhTables.neighbourhoods.ddl.drop
        }
    }
}