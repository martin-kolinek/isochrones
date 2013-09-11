package org.isochrone.db

import org.scalatest.FunSuite
import scala.io.Source
import java.io.BufferedInputStream
import java.util.zip.GZIPInputStream
import resource._
import java.io.Reader
import java.io.InputStreamReader

class TestDatabaseComponentTest extends FunSuite {
	test("test create and drop test db") {
		val comp = new TestDatabaseComponent {}
		try {
		    comp.init()
		}
		finally {
			comp.close()		    
		}
	}
}