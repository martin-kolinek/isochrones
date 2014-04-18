package org.isochrone.areas.pseudoconvex

import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.osm.CostAssignerComponent
import org.isochrone.graphlib.GraphComponentBase
import com.vividsolutions.jts.geom.Geometry
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.db.SessionProviderComponent
import org.isochrone.areas.PosAreaComponent
import org.isochrone.areas.AreaReaderComponent
import org.isochrone.areas.DbAreaReaderComponent
import scala.util.Try
import scala.util.Failure

trait AreaFixerComponent extends PosAreaComponent with GraphComponentBase {
    self: TriangulatorComponent with EdgeCostResolverComponent with ConvexizerComponent with AreaReaderComponent with DatabaseProvider with RoadNetTableComponent with CostAssignerComponent with ShrinkingRatioComponent =>

    override type NodeType <: Long

    object AreaFixer extends Logging {

        private val shrinkAmounts = amounts

        def getEdges(unshrinked: PosArea)(implicit s: Session) = {
            def getEdges(ar: PosArea) = {
                logger.debug(s"area = $ar")
                logger.debug(s"norm = ${ar.normalize}")
                if (!ar.toLinearRing.isValid)
                    throw new Exception(s"Area $ar not forming valid linear ring")
                logger.debug("triangulating")
                val diagonals = triangulator.triangulate(ar)
                logger.debug("resolving edges")
                val diagsWithCosts = resolver.resolve(diagonals)
                logger.debug("filtering")
                convexizer.convexize(ar, diagsWithCosts)
            }

            val fail: Try[Traversable[EdgeWithCost]] = Try(throw new Exception("No shrinkAmounts specified"))

            (fail /: shrinkAmounts) { (t, shrinkAmount) =>
                t.orElse(Try(getEdges(unshrinked.shrink(shrinkAmount))))
            }.get
        }

        def fixAreas() {
            database.withTransaction { implicit s: Session =>
                var i = 1
                for (ar <- reader.areas) {
                    logger.info(s"Working on area nr. $i with size ${ar.points.size}")
                    i += 1

                    val filtered = getEdges(ar)

                    def q(n1: Long, n2: Long) = for {
                        nd1 <- roadNetTables.roadNodes if nd1.id === n1
                        nd2 <- roadNetTables.roadNodes if nd2.id === n2
                    } yield (nd1.id, nd2.id, getNoRoadCost(nd1.geom, nd2.geom), true, nd1.geom.shortestLine(nd2.geom).asColumnOf[Geometry])
                    for {
                        e <- filtered
                        Seq(a, b) = e.nds.toSeq
                    } {
                        logger.info(s"Adding edge $a, $b")
                        roadNetTables.roadNet.insert(q(a, b))
                        roadNetTables.roadNet.insert(q(b, a))
                    }
                    logger.debug("Done with this area")
                }
                logger.debug("Done with cycle")
            }
        }
    }
}

trait AreaFixerReaderComponent extends DbAreaReaderComponent with AreaNormalizerComponent with PosAreaComponent {
    self: RoadNetTableComponent with GraphComponentBase with SessionProviderComponent with ShrinkingRatioComponent =>

    val reader = new DbAreaReader with AreaNormalizer
}