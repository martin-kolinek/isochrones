package org.isochrone.osm

import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.VisualizationTableComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import scala.slick.jdbc.{ StaticQuery => Q }
import org.isochrone.ActionComponent

trait RoadNetPrimaryKeyCreator {
    self: RoadNetTableComponent =>
    def createRoadNetPrimaryKey(implicit s: Session) {
        val rnet = roadNetTables.roadNet.tableName
        Q.updateNA(s"""ALTER TABLE "$rnet" ADD CONSTRAINT "${rnet}_pk" PRIMARY KEY (start_node, end_node)""").execute()
    }

    def dropRoadNetPrimaryKey(implicit s: Session) {
        val rnet = roadNetTables.roadNet.tableName
        Q.updateNA(s"""ALTER TABLE "$rnet" DROP CONSTRAINT "${rnet}_pk"""").execute()
    }
}

trait TableCreatorComponent extends RoadNetPrimaryKeyCreator {
    self: RoadNetTableComponent with VisualizationTableComponent with DatabaseProvider =>

    object tableCreator {
        def create() {
            database.withTransaction {
                implicit s: Session =>
                    roadNetTables.roadNet.ddl.create
                    roadNetTables.roadNodes.ddl.create
                    roadNetTables.roadRegions.ddl.create
                    visualizationTables.roadNetVisualization.ddl.create
                    roadNetTables.roadAreas.ddl.create
                    roadNetTables.areaGeoms.ddl.create
                    createRoadNetPrimaryKey
                    Q.updateNA(s"""CREATE INDEX "ix_${visualizationTables.roadNetVisualization.tableName}" ON "${visualizationTables.roadNetVisualization.tableName}" using GIST (linestring)""").execute
                    Q.updateNA(s"""CREATE INDEX "ix_${roadNetTables.roadNodes.tableName}" ON "${roadNetTables.roadNodes.tableName}" using GIST (geom)""").execute
                    Q.updateNA(s"""CREATE INDEX "ix_${roadNetTables.roadNodes.tableName}_region_id" ON "${roadNetTables.roadNodes.tableName}"(region, id)""").execute()
                    Q.updateNA(s"""CREATE INDEX "ix_${roadNetTables.roadNet.tableName}" ON "${roadNetTables.roadNet.tableName}" using GIST (geom)""").execute()
            }
        }
        def drop() {
            database.withTransaction {
                implicit s: Session =>
                    roadNetTables.roadNet.ddl.drop
                    roadNetTables.roadNodes.ddl.drop
                    roadNetTables.roadRegions.ddl.drop
                    roadNetTables.roadAreas.ddl.drop
                    visualizationTables.roadNetVisualization.ddl.drop
                    roadNetTables.areaGeoms.ddl.drop

            }
        }

    }
}