package org.isochrone.hh

import org.isochrone.db.DatabaseProvider
import scala.slick.jdbc.{ StaticQuery => Q }
import org.isochrone.util.db.MyPostgresDriver.simple._

trait HHTableCreatorComponent {
    self: HHTableComponent with DatabaseProvider =>
    object HHTableCreator {
        private def ddls = (hhTables.neighbourhoods.ddl ++ hhTables.shortcutEdges.ddl ++ hhTables.reverseNeighbourhoods.ddl ++ hhTables.descendLimit.ddl ++ hhTables.shortcutReverseLimit.ddl ++ hhTables.reverseShortcutEdges.ddl)

        def createTables() = database.withSession { implicit s: Session =>
            ddls.create
            Q.updateNA(s"""ALTER TABLE "${hhTables.shortcutEdges}" ADD CONSTRAINT "${hhTables.shortcutEdges}_pk" PRIMARY KEY (start_node, end_node)""").execute()
            Q.updateNA(s"""ALTER TABLE "${hhTables.reverseShortcutEdges}" ADD CONSTRAINT "${hhTables.reverseShortcutEdges}_pk" PRIMARY KEY (start_node, end_node)""").execute()
        }

        def dropTables() = database.withSession { implicit s: Session =>
            ddls.drop
        }
    }
}