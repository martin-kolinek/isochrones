package org.isochrone.db

import org.isochrone.util.db.MyPostgresDriver.simple._
import slick.jdbc.StaticQuery.interpolation
import com.vividsolutions.jts.geom.Geometry
import org.isochrone.OptionParserComponent
import scopt.OptionParser
import org.isochrone.ArgumentParser

trait RegularPartitionComponent {
    self: RoadNetTableComponent with DatabaseProvider =>

    class RegularPartition(regionSize: Double) {
        case class BoundingBox(top: Double, left: Double, bottom: Double, right: Double) {
            def withBuffer(regions: Int): BoundingBox = BoundingBox(top + regions * regionSize, left - regions * regionSize, bottom - regions * regionSize, right + regions * regionSize)

            def dbBBox: Column[Geometry] = makeBox(makePoint(left, bottom), makePoint(right, top))
        }
        val (top, left, bottom, right) = database.withSession { implicit s: Session =>
            sql"SELECT MAX(ST_Y(geom)), MIN(ST_X(geom)), MIN(ST_Y(geom)), MAX(ST_X(geom)) FROM #${roadNetTables.roadNodes.baseTableRow.tableName}".as[(Double, Double, Double, Double)].first

        }

        def regions = for {
            x <- left - regionSize / 10 to right by regionSize
            y <- bottom - regionSize / 10 to top by regionSize
        } yield BoundingBox(y + regionSize, x, y, x + regionSize)

        val regionCount = regions.size
    }

    val regularPartition: RegularPartition
}

trait RegularPartitionParserComponent extends OptionParserComponent {
    lazy val regionSizeLens = registerConfig(0.5)

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[Double]("region-size").action((x, c) => regionSizeLens.set(c)(x)).
            text("the size of regions used in incremental actions (default = 0.5 degree)")
    }
}

trait ConfigRegularPartitionComponent extends RegularPartitionParserComponent with RegularPartitionComponent {
    self: ArgumentParser with RoadNetTableComponent with DatabaseProvider =>

    val regularPartition = new RegularPartition(regionSizeLens.get(parsedConfig))
}

trait BufferOptionParserComponent extends OptionParserComponent {
    lazy val bufferSizeLens = registerConfig(2)

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[Int]("buffer-size").action((x, c) => bufferSizeLens.set(c)(x)).
            text("the buffer size used in incremental partitioner (default = 2)")
    }
}