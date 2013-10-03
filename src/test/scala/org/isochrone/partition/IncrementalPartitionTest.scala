package org.isochrone.partition

import org.scalatest.FunSuite
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DefaultRoadNetTablesWithPrefix
import org.isochrone.db.TestDatabaseComponent
import org.isochrone.db.TestDatabase
import org.isochrone.db.RegularPartitionComponent
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.dbgraph.WithoutRegionsBoundedGraphComponent
import org.isochrone.graphlib.GraphComponent

class IncrementalPartitionTest extends FunSuite with TestDatabase {
    test("IncrementalPartitioner works") {
        new IncrementalPartitionComponent with RoadNetTableComponent with TestDatabaseComponent with RegularPartitionComponent with BBoxPartitionerProvider with WithoutRegionsBoundedGraphComponent {
            def createPartitioner(bbox: regularPartition.BoundingBox) = new PartitionerComponent with GraphComponent {
                type NodeType = Long
                val graph = WithoutRegionsBoundedGraphCreator.createGraph(bbox)
                val partitioner = new Partitioner {
                    def partition() = {
                        val realPartition = Set(Set(1l, 2l, 3l, 4l), Set(5l, 6l), Set(7l, 8l, 9), Set(10l, 11l))
                        realPartition.map(_.intersect(graph.nodes.toSet)).filter(_.nonEmpty)
                    }
                }
            }
            val roadNetTables = new DefaultRoadNetTablesWithPrefix("incpart_")
            val partitioner = new IncrementalPartitioner(1)
            val regularPartition = new RegularPartition(1)

            partitioner.partition()

            database.withSession { implicit s: Session =>
                val lst = Query(roadNetTables.roadNodes).map(x => (x.id, x.region)).list
                val regions =
                    lst.groupBy(_._2).map {
                        case (reg, nodes) => nodes.map(_._1).toSet
                    }.toSet
                info(regions.toString)
                assert(regions == Set(Set(1l, 2l, 3l, 4l), Set(5l, 6l), Set(7l, 8l, 9l), Set(10l), Set(11l)))
            }
        }

    }
}