package org.isochrone.dbgraph

import org.scalatest.FunSuite
import scala.slick.driver.SQLiteDriver.simple._

class DbGraphTest extends FunSuite {
	def initDB(func: GraphTables => Session => Unit) {
		val db = Database.forURL("jdbc:sqlite::memory:", driver = "org.sqlite.JDBC")
		db.withSession { implicit session:Session =>
			val tbls = new GraphTables("nodes", "edges")  
		    (tbls.nodes.ddl ++ tbls.edges.ddl).create
		    func(tbls)(session)
		}
	}
	
	test("DatabaseGraph retrieves neighbours") {
		initDB { tbls:GraphTables => implicit session:Session =>
			(1l to 5l).map((_, 1)).foreach(tbls.nodes.insert(_))
			tbls.edges.insertAll(
				(1, 2, 0.1),
				(1, 3, 0.2),
				(2, 4, 0.3),
				(3, 2, 0.4),
				(2, 1, 0.5),
				(4, 5, 0.6))
			
			val g = new DatabaseGraph(tbls, 1)
			val neigh = g.getNeighbours(2)
			assert(neigh.size==2)
			assert(neigh.toSet == Set((1, 0.5), (4, 0.3)))
		}
	}
	
	test("DatabaseGraph return empty list for nonexistent nodes)") {
		initDB { tbls:GraphTables => implicit session:Session =>
			(1l to 5l).map((_, 1)).foreach(tbls.nodes.insert(_))
			tbls.edges.insertAll(
				(1, 2, 0.1),
				(1, 3, 0.2),
				(2, 4, 0.3),
				(3, 2, 0.4),
				(2, 1, 0.5),
				(4, 5, 0.6))
			
			val g = new DatabaseGraph(tbls, 1)
			assert(g.getNeighbours(5).size==0)
			val neigh = g.getNeighbours(10)
			assert(neigh.size==0)
		}
	}
	
	test("DatabaseGraph keeps right amount of regions") {
		initDB{ tbls:GraphTables => implicit session:Session =>
		    tbls.nodes.insertAll(
		    		(1, 1),
		    		(2, 1),
		    		(3, 2),
		    		(4, 2),
		    		(5, 2))
		    tbls.edges.insertAll(
		    		(1, 2, 0.1),
		    		(2, 3, 0.2),
		    		(2, 4, 0.3),
		    		(5, 2, 0.4),
		    		(5, 3, 0.5),
		    		(4, 5, 0.6),
		    		(3, 5, 0.7))
		    val g = new DatabaseGraph(tbls, 1)
		    val neigh = g.getNeighbours(2)
		    assert(neigh.size==2)
		    assert(neigh.toSet==Set((3, 0.2), (4, 0.3)))
		    assert(g.nodesInMemory==2)
		    val neigh2 = g.getNeighbours(5)
		    assert(neigh2.size==2)
		    assert(neigh2.toSet==Set((2, 0.4), (3, 0.5)))
		    assert(g.nodesInMemory==3)
		}
	}
}