package org.isochrone.db

import org.isochrone.util.db.MyPostgresDriver.simple._
import com.vividsolutions.jts.geom.LineString
import org.isochrone.ArgumentParser

trait VisualizationTableComponent {
	class RoadNetVisualization(tag:Tag, name:String) extends Table[(Long, Long, Int, LineString)](tag, name) {
	    def start = column[Long]("start_node")
	    def end = column[Long]("end_node")
	    def direction = column[Int]("direction")
	    def linestring = column[LineString]("linestring")
	    def * = (start, end, direction, linestring)
	}
	
	trait VisualizationTables {
	    val roadNetVisualization: TableQuery[RoadNetVisualization]
	}
	
	val visualizationTables: VisualizationTables
}

trait DefaultVisualizationTableComponent extends VisualizationTableComponent {
    val visualizationTables = new VisualizationTables {
        val roadNetVisualization = TableQuery(t => new RoadNetVisualization(t, "road_net_vis"))
    }
}

trait ConfigVisualizationTableComponent extends VisualizationTableComponent with RoadNetTableParsingComponent {
    self: ArgumentParser =>
    val visualizationTables = new VisualizationTables {
        val roadNetVisualization = TableQuery(t => new RoadNetVisualization(t, roadNetPrefixLens.get(parsedConfig) + "road_net_vis"))
    }
}