package org.isochrone.osm

import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import slick.jdbc.StaticQuery.interpolation
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.db.VisualizationTableComponent

trait DuplicitRemoverComponent extends RoadNetPrimaryKeyCreator {
    self: RoadNetTableComponent with DatabaseProvider =>

    object DuplicitRemover extends Logging {
        def removeDuplicates() {
            database.withTransaction { implicit s: Session =>
                dropRoadNetPrimaryKey
                val rnet = roadNetTables.roadNet.tableName
                val rnodes = roadNetTables.roadNodes.tableName
                Query(roadNetTables.roadNet).filter(x => x.start === x.end).delete
                sqlu"""UPDATE "#$rnet" SET start_node = nn.id 
                       from "#$rnodes" oldn, "#$rnodes" nn 
                       where nn.id < oldn.id and ST_Equals(nn.geom, oldn.geom) and "#$rnet".start_node = oldn.id""".execute
                Query(roadNetTables.roadNet).filter(x => x.start === x.end).delete
                sqlu"""UPDATE "#$rnet" SET end_node = nn.id 
                       from "#$rnodes" oldn, "#$rnodes" nn 
                       where nn.id < oldn.id and ST_Equals(nn.geom, oldn.geom) and "#$rnet".end_node = oldn.id""".execute
                Query(roadNetTables.roadNet).filter(x => x.start === x.end).delete

                val q = for {
                    on <- roadNetTables.roadNodes
                    if Query(roadNetTables.roadNodes).
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