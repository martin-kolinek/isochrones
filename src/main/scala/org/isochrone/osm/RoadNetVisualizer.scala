package org.isochrone.osm

import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.OsmTableComponent
import org.isochrone.db.VisualizationTableComponent
import scala.slick.jdbc.{ StaticQuery => Q }
import org.isochrone.ActionComponent

trait RoadNetVisualizerComponent {
    self: RoadNetTableComponent with DatabaseProvider with VisualizationTableComponent =>
    object visualizer {
        def execute() {
            database.withTransaction { implicit s: Session =>
                def insertQuery(out: String, rnet: String, nodes: String) = s"""
insert into "$out"(start_node, end_node, direction, linestring)
    select sn.id, en.id, prn.direction, ST_MakeLine(sn.geom, en.geom) 
        from (select distinct rn.start_node, rn.end_node, case coalesce(rn2.start_node, 1) when 1 then 1 else 0 end as direction 
                from "$rnet" rn 
                    left join "$rnet" rn2 on rn2.start_node=rn.end_node and rn2.end_node=rn.start_node and rn2.virtual = false where rn.virtual = false) prn
            inner join "$nodes" sn on sn.id = prn.start_node 
            inner join "$nodes" en on en.id = prn.end_node
        where prn.direction = 1 or prn.start_node<prn.end_node
    union 
    select distinct on (rn.start_node, rn.end_node) rn.start_node, rn.end_node, 2, rn.geom
        from "$rnet" rn inner join
             "$rnet" rn2 on rn.start_node = rn2.end_node and rn.end_node = rn2.start_node
        where rn.virtual = true and rn2.virtual = true and (rn.start_node < rn2.start_node or (rn.start_node = rn2.start_node and rn.end_node < rn2.end_node));"""
                visualizationTables.roadNetVisualization.delete
                Q.updateNA(insertQuery(visualizationTables.roadNetVisualization.baseTableRow.tableName, roadNetTables.roadNet.baseTableRow.tableName, roadNetTables.roadNodes.baseTableRow.tableName)).execute
            }
        }
    }
}