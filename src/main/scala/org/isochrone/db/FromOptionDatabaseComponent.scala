package org.isochrone.db

import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.ArgumentsProvider
import org.isochrone.ArgumentParser

trait FromOptionDatabaseComponent extends DatabaseProvider {
    self: DatabaseOptionParsingComponent with ArgumentParser =>

    def database = Database.forURL("jdbc:postgresql:%s".format(dbNameLens.get(parsedConfig)), driver = "org.postgresql.Driver")
}