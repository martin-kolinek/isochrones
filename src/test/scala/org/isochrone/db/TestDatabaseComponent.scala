package org.isochrone.db
import resource._

import scala.io.Source
import java.util.zip.GZIPInputStream
import java.io.BufferedInputStream
import scala.sys.process._
import org.isochrone.util.db.MyPostgresDriver.simple._

trait TestDatabaseComponent extends DatabaseProvider {
    def database = Database.forURL("jdbc:postgresql:test_isochrones_db", driver = "org.postgresql.Driver")
}
