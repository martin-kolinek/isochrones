package org.isochrone.partition

import org.isochrone.db.RegularPartitionComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import com.typesafe.scalalogging.slf4j.Logging

trait QuickPartitionComponent {
    self: RegularPartitionComponent with RoadNetTableComponent with DatabaseProvider =>

    object Partitioner extends Logging {
        def partition() = {
            database.withTransaction { implicit s =>
                regularPartition.regions.zipWithIndex.foreach {
                    case (reg, idx) => {
                        logger.debug(s"Processing region $idx/${regularPartition.regionCount}")
                        val q = roadNetTables.roadNodes.filter(_.geom @&& reg.dbBBox).map(_.region).update(idx + 1)
                    }
                }
            }
        }
    }
}