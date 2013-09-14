package org.isochrone.db

import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.ArgumentsProvider

trait FromOptionDatabaseComponent extends DatabaseProvider {
    self: DatabaseOptionParsingComponent with ArgumentsProvider =>
        
    val cfg = parser.parse(args, parserStart).getOrElse(throw new Exception("Command line arguments not parsed"))
        
    def database = Database.forURL("jdbc:postgresql:%s".format(cfg.database), driver = "org.postgresql.Driver")
}