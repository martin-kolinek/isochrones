package org.isochrone.areas.pseudoconvex

import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.osm.CostAssignerComponent
import org.isochrone.graphlib.GraphComponentBase
import com.vividsolutions.jts.geom.Geometry
import com.typesafe.scalalogging.slf4j.Logging

trait AreaFixerComponent extends PosAreaComponent {
    self: TriangulatorComponent with GraphComponentBase with EdgeCostResolverComponent with ConvexizerComponent with AreaReaderComponent with DatabaseProvider with RoadNetTableComponent with CostAssignerComponent with AreaShrinkerComponent =>

    type NodeType <: Long

    object AreaFixer extends Logging {
        def fixAreas() {
            database.withTransaction { implicit s: Session =>
                var i = 1
                for (ar <- shrinkedReader.areas) {
                    logger.info(s"Working on area nr. $i")
                    i += 1
                    if (!ar.toLinearRing.isValid)
                        throw new Exception(s"Area $ar not forming valid linear ring")
                    val diagonals = triangulator.triangulate(ar)
                    val diagsWithCosts = resolver.resolve(diagonals)
                    val filtered = convexizer.convexize(ar, diagsWithCosts)

                    def q(n1: Long, n2: Long) = for {
                        nd1 <- roadNetTables.roadNodes if nd1.id === n1
                        nd2 <- roadNetTables.roadNodes if nd2.id === n2
                    } yield (nd1.id, nd2.id, getNoRoadCost(nd1.geom, nd2.geom), true, nd1.geom.shortestLine(nd2.geom).asColumnOf[Geometry])
                    for {
                        e <- filtered
                        Seq(a, b) <- e.nds.toSeq
                    } {
                        roadNetTables.roadNet.insert(q(a.asInstanceOf[Long], b.asInstanceOf[Long]))
                        roadNetTables.roadNet.insert(q(b.asInstanceOf[Long], a.asInstanceOf[Long]))
                    }
                }
            }
        }
    }
}