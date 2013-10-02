package org.isochrone.db

import org.isochrone.util.db.MyPostgresDriver.simple._
import slick.jdbc.StaticQuery.interpolation
import com.vividsolutions.jts.geom.Geometry

trait RegularPartitionComponent {
    self: RoadNetTableComponent with DatabaseProvider =>

    class RegularPartition(regionSize: Double) {
        case class BoundingBox(top: Double, left: Double, bottom: Double, right: Double) {
            def withBuffer(regions: Int): BoundingBox = BoundingBox(top + regionSize, left - regionSize, bottom - regionSize, right + regionSize)

            def dbBBox: Column[Geometry] = makeBox(makePoint(left, bottom), makePoint(right, top))
        }
        val (top, left, bottom, right) = database.withSession { implicit s: Session =>
            sql"SELECT MAX(ST_Y(geom), MIN(ST_X(geom)), MIN(ST_Y(geom)), MAX(ST_X(geom)) FROM #${roadNetTables.roadNodes.tableName}".as[(Double, Double, Double, Double)].first
        }

        def regions: Traversable[BoundingBox] = for {
            x <- left to right by regionSize
            y <- bottom to top by regionSize
        } yield BoundingBox(y + regionSize, x, y, x + regionSize)
    }

    val regularPartition: RegularPartition
}