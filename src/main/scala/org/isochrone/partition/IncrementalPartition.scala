package org.isochrone.partition

import org.isochrone.partition.merging.MergingPartitionerProvider
import org.isochrone.db.RegularPartitionComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.util.db.MyPostgresDriver.simple._

trait IncrementalPartitionComponent {
    self: MergingPartitionerProvider with RegularPartitionComponent with RoadNetTableComponent with DatabaseProvider =>

    class IncrementalPartitioner(bufferSize: Int) {
        def partition() {
            database.withSession { implicit s: Session =>
                for (bbox <- regularPartition.regions) {
                    val bboxNodes = roadNetTables.roadNodes.filter(_.geom @&& bbox.dbBBox).map(_.id).to[Set]
                    val partition = createPartitioner(bbox.withBuffer(bufferSize)).MergingPartitioner.partition()

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
}