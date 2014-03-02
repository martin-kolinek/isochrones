package org.isochrone.partition

import org.isochrone.db.RegularPartitionComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.OptionParserComponent
import scopt.OptionParser
import org.isochrone.ArgumentParser
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.db.BufferOptionParserComponent

trait IncrementalPartitionComponent {
    self: BBoxPartitionerProvider with RegularPartitionComponent with RoadNetTableComponent with DatabaseProvider =>

    class IncrementalPartitioner(bufferSize: Int) extends Logging {
        def partition() {
            database.withTransaction { implicit s: Session =>
                roadNetTables.roadNodes.map(_.region).update(0)
            }
            database.withTransaction { implicit s: Session =>

                for ((bbox, i) <- regularPartition.regions.zipWithIndex) {
                    logger.info(s"Partitioning region $bbox ($i/${regularPartition.regionCount})")
                    val bboxNodes = roadNetTables.roadNodes.filter(_.geom @&& bbox.dbBBox).map(_.id).to[Set]
                    val partition = createPartitioner(bbox.withBuffer(bufferSize)).partitioner.partition()
                    val regionsInBBox = partition.filter(_.exists(bboxNodes.contains)).toSeq
                    val maxRegionNum = Query(roadNetTables.roadNodes.map(_.region).max).first.getOrElse(0)
                    for {
                        (rg, i) <- regionsInBBox.zipWithIndex
                        id = (maxRegionNum + 1l + i).toInt
                    } {
                        roadNetTables.roadNodes.filter(_.id.inSet(rg)).map(_.region).update(id)
                    }
                }
            }
        }
    }

    val partitioner: IncrementalPartitioner
}

trait ConfigIncrementalPartitionComponent extends IncrementalPartitionComponent with BufferOptionParserComponent {
    self: BBoxPartitionerProvider with RegularPartitionComponent with RoadNetTableComponent with DatabaseProvider with ArgumentParser =>

    val partitioner = new IncrementalPartitioner(bufferSizeLens.get(parsedConfig))
}