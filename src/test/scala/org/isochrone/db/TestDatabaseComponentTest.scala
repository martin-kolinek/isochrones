package org.isochrone.db

import org.scalatest.FunSuite
import scala.io.Source
import java.io.BufferedInputStream
import java.util.zip.GZIPInputStream
import resource._
import java.io.Reader
import java.io.InputStreamReader
import org.isochrone.util.db.MyPostgresDriver.simple._
import scala.slick.jdbc.StaticQuery.interpolation

class TestDatabaseComponentTest extends FunSuite with TestDatabase {
	test("TestDatabaseComponent works") {
		val comp = new TestDatabaseComponent {
		    database.withSession { implicit s:Session =>
		        assert(sql"SELECT 1".as[Int].first==1)
		    }
		}
	}
}