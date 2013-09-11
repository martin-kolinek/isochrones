package org.isochrone.db

import org.isochrone.util.db.MyPostgresDriver.simple._
import resource._
import scala.io.Source
import java.util.zip.GZIPInputStream
import java.io.BufferedInputStream
import scala.slick.jdbc.{ StaticQuery => Q }
import Q.interpolation
import scala.sys.process._
import org.isochrone.util.TemporaryFile

trait TestDatabaseComponent extends DatabaseProvider {
    def pg = Database.forURL("jdbc:postgresql:postgres", driver = "org.postgresql.Driver")
    def init() {
        pg.withSession { implicit s: Session =>
            sqlu"CREATE DATABASE test_isochrones_db WITH TEMPLATE test_isochrones_db_template".execute

        }
    }

    def close() {
        pg.withSession { implicit s: Session =>
            sqlu"DROP DATABASE test_isochrones_db".execute
        }
    }

    def database = Database.forURL("jdbc:postgresql:test_isochrones_db", driver = "org.postgresql.Driver")
}