package org.isochrone.db

import org.scalatest.BeforeAndAfterEach
import org.scalatest.Suite
import scala.slick.jdbc.{ StaticQuery => Q }
import Q.interpolation
import org.isochrone.util.db.MyPostgresDriver.simple._

trait TestDatabase extends BeforeAndAfterEach {
    self: Suite =>

    def pg = Database.forURL("jdbc:postgresql:postgres", driver = "org.postgresql.Driver")

    abstract override def beforeEach() {
        super.beforeEach()
        pg.withSession { implicit s: Session =>
            sqlu"CREATE DATABASE test_isochrones_db WITH TEMPLATE test_isochrones_db_template".execute

        }
    }

    abstract override def afterEach() {
        super.afterEach()
        pg.withSession { implicit s: Session =>
            sqlu"""
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'test_isochrones_db'
  AND pid <> pg_backend_pid();""".execute
            sqlu"DROP DATABASE test_isochrones_db".execute
        }
    }
}