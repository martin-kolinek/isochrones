package org.isochrone.osm

import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import slick.jdbc.StaticQuery.interpolation
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.db.VisualizationTableComponent
import com.vividsolutions.jts.geom.Geometry

trait DuplicitRemoverComponent extends RoadNetPrimaryKeyCreator {
    self: RoadNetTableComponent with DatabaseProvider =>

    object DuplicitRemover extends Logging {
        def removeDuplicates() {
            database.withTransaction { implicit s: Session =>
                dropRoadNetPrimaryKey
                val rnet = roadNetTables.roadNet.baseTableRow.tableName
                val rnodes = roadNetTables.roadNodes.baseTableRow.tableName
                roadNetTables.roadNet.filter(x => x.start === x.end).delete

                sqlu"""UPDATE "#$rnet" SET start_node = nn.id 
                       from (select oldn.id as oldid, min(n.id) as id 
                           from #$rnodes n 
                           inner join #$rnodes oldn 
                           on st_equals(oldn.geom, n.geom) 
                           group by oldn.id) nn 
                           where nn.oldid = #$rnet.start_node 
                       """.execute
                roadNetTables.roadNet.filter(x => x.start === x.end).delete
                sqlu"""UPDATE "#$rnet" SET end_node = nn.id 
                       from (select oldn.id as oldid, min(n.id) as id 
                           from #$rnodes n 
                           inner join #$rnodes oldn 
                           on st_equals(oldn.geom, n.geom) 
                           group by oldn.id) nn 
                           where nn.oldid = #$rnet.end_node
                       """.execute
                roadNetTables.roadNet.filter(x => x.start === x.end).delete

                val q = for {
                    on <- roadNetTables.roadNodes
                    if roadNetTables.roadNodes.
                        filter(_.geom.gEquals(on.geom)).
                        filter(_.id < on.id).
                        exists
                } yield on
                q.delete

                sqlu"""CREATE TABLE "tmp_#$rnet" AS SELECT DISTINCT ON (start_node, end_node) start_node, end_node, cost, virtual, geom FROM #$rnet""".execute()
                sqlu"""DROP TABLE "#$rnet"""".execute()
                sqlu"""ALTER TABLE "tmp_#$rnet" RENAME TO "#$rnet"""".execute()
                createRoadNetPrimaryKey
            }
        }
    }

}