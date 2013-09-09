package org.isochrone.osm

import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.OsmTableComponent
import org.isochrone.db.VisualizationTableComponent

trait RoadNetVisualizer {
	self: RoadNetTableComponent with DatabaseProvider with OsmTableComponent with VisualizationTableComponent =>
	object Visualizer {
	    def execute() {
	        database.withTransaction { implicit s:Session =>
	            def visBase(net:EdgeTable) = for {
	                (n1, n2) <- net.leftJoin(net).on((x, y) => x.start === y.end && x.end === y.start)
	            } yield (n1.start, n1.end, (n2.start.ifNull(0).sign - 1) * -1)
	            
	            def vis(net:EdgeTable) = for {
	                (s, e, dir) <- visBase(net).groupBy(x=>x).map(_._1)
	                sn <- osmTables.nodes if sn.id === s
	                en <- osmTables.nodes if en.id === e
	                if dir === 1 || s < e
	            } yield (s, e, dir, sn.geom.shortestLine(en.geom))
	            
	            visualizationTables.roadNetVisualization.insert(vis(roadNetTables.roadNet))
	            visualizationTables.roadNetUndirVisualization.insert(vis(roadNetTables.roadNetUndir))
	        }
	    }
	}
}