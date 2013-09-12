package org.isochrone.osm

import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.VisualizationTableComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import scala.slick.jdbc.{ StaticQuery => Q }
import org.isochrone.ActionComponent

trait TableCreatorComponent extends ActionComponent {
    self: RoadNetTableComponent with VisualizationTableComponent with DatabaseProvider =>

    object tableCreator extends Executor {
        def execute() {
            database.withTransaction {
                implicit s: Session =>
                    roadNetTables.roadNet.ddl.create
                    roadNetTables.roadNetUndir.ddl.create
                    roadNetTables.roadNodes.ddl.create
                    visualizationTables.roadNetVisualization.ddl.create
                    visualizationTables.roadNetUndirVisualization.ddl.create
                    for (tbl <- Seq(visualizationTables.roadNetVisualization, visualizationTables.roadNetUndirVisualization))
                        Q.updateNA(s"""CREATE INDEX "ix_${tbl.tableName}" ON ${tbl.tableName} using GIST (linestring)""").execute
            }
        }
    }
    object tableDropper extends Executor {
        def execute() {
            database.withTransaction {
                implicit s: Session =>
                    roadNetTables.roadNet.ddl.drop
                    roadNetTables.roadNetUndir.ddl.drop
                    roadNetTables.roadNodes.ddl.drop
                    visualizationTables.roadNetVisualization.ddl.drop
                    visualizationTables.roadNetUndirVisualization.ddl.drop
                    for (tbl <- Seq(visualizationTables.roadNetVisualization, visualizationTables.roadNetUndirVisualization))
                        Q.updateNA(s"""DROP INDEX "ix_${tbl.tableName}" """)
            }
        }
    }
}