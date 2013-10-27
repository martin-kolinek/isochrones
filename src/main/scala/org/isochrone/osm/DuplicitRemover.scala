package org.isochrone.osm

import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import slick.jdbc.StaticQuery.interpolation

trait DuplicitRemoverComponent {
    self: RoadNetTableComponent with DatabaseProvider =>

    object DuplicitRemover {
        def removeDupEdges() {
            database.withTransaction { implicit s: Session =>
                val q = for {
                    r <- roadNetTables.roadNet
                    if Query(roadNetTables.roadNet).
                        filter(_.geom.gEquals(r.geom)).
                        filter(x => x.start < r.start || x.start === r.start && x.end < r.end).
                        exists
                } yield r
                q.delete
            }
        }

        def removeDupNodes() {
            database.withTransaction { implicit s: Session =>
                val rnet = roadNetTables.roadNet.tableName
                val rnodes = roadNetTables.roadNodes.tableName
                sqlu"""UPDATE "#$rnet" SET start_node = nn.id 
                       from "#$rnodes" oldn, "#$rnodes" nn 
                       where nn.id < oldn.id and ST_Equals(nn.geom, oldn.geom) and "#$rnet".start_node = oldn.id""".execute
                sqlu"""UPDATE "#$rnet" SET end_node = nn.id 
                       from "#$rnodes" oldn, "#$rnodes" nn 
                       where nn.id < oldn.id and ST_Equals(nn.geom, oldn.geom) and "#$rnet".end_node = oldn.id""".execute
                val q = for {
                    on <- roadNetTables.roadNodes
                    if Query(roadNetTables.roadNodes).
                        filter(_.geom.gEquals(on.geom)).
                        filter(_.id < on.id).
                        exists
                } yield on
                q.delete
            }
        }
    }

}