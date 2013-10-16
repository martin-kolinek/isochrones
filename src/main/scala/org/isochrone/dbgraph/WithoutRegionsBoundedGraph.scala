package org.isochrone.dbgraph

import org.isochrone.db.RegularPartitionComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.graphlib.GraphType
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.simplegraph.SimpleGraphComponent
import org.isochrone.dijkstra.DefaultDijkstraProvider
import org.isochrone.dijkstra.DijkstraProvider

trait WithoutRegionsBoundedGraphComponent extends GraphComponentBase with SimpleGraphComponent with DefaultDijkstraProvider {
    self: RegularPartitionComponent with DatabaseProvider with RoadNetTableComponent =>

    type NodeType = Long

    object WithoutRegionsBoundedGraphCreator {
        def createGraph(bbox: regularPartition.BoundingBox): GraphType[NodeType] = {
            val q = for {
                e <- roadNetTables.roadNet
                n1 <- roadNetTables.roadNodes if n1.id === e.start
                n2 <- roadNetTables.roadNodes if n2.id === e.end
                if (bbox.dbBBox @&& n1.geom) && (bbox.dbBBox @&& n2.geom)
                if n1.region === 0 && n2.region === 0
            } yield (n1.id, n2.id, e.cost)
            database.withSession { implicit s: Session =>
                SimpleGraph(q.list: _*)
            }
        }
    }
}