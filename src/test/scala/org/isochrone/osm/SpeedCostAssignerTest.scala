package org.isochrone.osm

import org.scalatest.FunSuite
import org.isochrone.db.TestDatabase
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.DefaultRoadNetTablesWithPrefix
import org.scalatest.matchers.MustMatchers
import org.isochrone.util._

class SpeedCostAssignerTest extends FunSuite with TestDatabase with MustMatchers {
    test("test harvesine") {
        val comp = new SpeedCostAssignerComponent with TestDatabaseComponent {
            def noRoadSpeed = 4.0
            def roadSpeed = 70.0

            val rnet = new DefaultRoadNetTablesWithPrefix("ar_")

            database.withTransaction { implicit s =>
                val q = for {
                    n1 <- rnet.roadNodes
                    n2 <- rnet.roadNodes
                } yield (n1.geom, n2.geom, getNoRoadCost(n1.geom, n2.geom), getRoadCost(n1.geom, n2.geom))
                q.list.foreach {
                    case (g1, g2, nrcost, rcost) => {
                        val p1 = g1.getInteriorPoint
                        val p2 = g2.getInteriorPoint
                        val p1v = vector(p1.getX, p1.getY)
                        val p2v = vector(p2.getX, p2.getY)
                        info(s"dist: ${Harvesine.distance(p1v, p2v)}, p1:$p1, p2:$p2")
                        getNoDbRoadCost(p1v, p2v) must be(rcost plusOrMinus 1e-3)
                        getNoDbNoRoadCost(p1v, p2v) must be(nrcost plusOrMinus 1e-3)
                    }
                }

            }
        }
    }
}