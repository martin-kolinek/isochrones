package org.isochrone.osm

import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.OsmTableComponent
import org.isochrone.db.VisualizationTableComponent
import scala.slick.jdbc.{ StaticQuery => Q }
import org.isochrone.ActionComponent

trait RoadNetVisualizerComponent extends ActionComponent {
    self: RoadNetTableComponent with DatabaseProvider with OsmTableComponent with VisualizationTableComponent =>
    object visualizer extends Executor {
        def execute() {
            database.withTransaction { implicit s: Session =>
                def insertQuery(out: String, rnet: String, nodes: String) = s"""
insert into "$out"(start_node, end_node, direction, linestring)
    select sn.id, en.id, prn.direction, ST_MakeLine(sn.geom, en.geom) 
        from (select distinct rn.start_node, rn.end_node, case coalesce(rn2.start_node, 1) when 1 then 1 else 0 end as direction 
                from "$rnet" rn 
                    left join "$rnet" rn2 on rn2.start_node=rn.end_node and rn2.end_node=rn.start_node) prn
            inner join "$nodes" sn on sn.id = prn.start_node 
            inner join "$nodes" en on en.id = prn.end_node
        where prn.direction = 1 or prn.start_node<prn.end_node;"""

                Q.updateNA(insertQuery(visualizationTables.roadNetVisualization.tableName, roadNetTables.roadNet.tableName, osmTables.nodes.tableName)).execute
                Q.updateNA(insertQuery(visualizationTables.roadNetUndirVisualization.tableName, roadNetTables.roadNetUndir.tableName, osmTables.nodes.tableName)).execute
            }
        }
    }
}