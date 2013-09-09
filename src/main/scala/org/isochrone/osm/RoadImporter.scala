package org.isochrone.osm

import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.OsmTableComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DatabaseProvider

trait RoadImporterComponent {
    self: OsmTableComponent with RoadNetTableComponent with DatabaseProvider =>
	
    object RoadImporter {
        val roadNetQuery = {
        	val roads = for {
        		w <- osmTables.ways 
        		if w.tags ?& "highway"
        		if w.tags +> "highway" =!= "path"
        		if w.tags +> "highway" =!= "cycleway"
        		if w.tags +> "highway" =!= "footway"
        		if w.tags +> "highway" =!= "bridleway"
        		if w.tags +> "highway" =!= "steps"
        		if w.tags +> "highway" =!= "pedestrian"
        		if w.tags +> "highway" =!= "proposed"
        	} yield w
        	
        	val roadEdgeJoin = for {
        		c <- osmTables.wayNodes
        		n <- osmTables.wayNodes if n.sequenceId === c.sequenceId + 1 && n.wayId === c.wayId
        		r <- roads if r.id === c.wayId
        	} yield (c, n, r)
        	
        	val roadNetBare = for {
        		(c, n, r) <- roadEdgeJoin
        		if !(r.tags ?& "oneway") || r.tags +> "oneway" =!= "-1"
        	} yield (c.nodeId, n.nodeId)
        	
        	val roadNetBareBack = for {
        		(c, n, r) <- roadEdgeJoin
        		if (!(r.tags ?& "oneway") 
        				&& r.tags +> "highway" =!= "motorway" 
        				&& r.tags +> "highway" =!= "motorway_link"
        				&& (!(r.tags ?& "junction") ||  r.tags +> "junction" =!= "roundabout")) ||
        			r.tags +> "oneway" === "-1" ||
        			r.tags +> "oneway" === "no"
        	} yield (n.nodeId, c.nodeId)
        	
        	val roadNetBareAll = roadNetBare union roadNetBareBack
        	
        	for {
        		(s, e) <- roadNetBareAll
        		sn <- osmTables.nodes if sn.id === s
        		en <- osmTables.nodes if en.id === e
        	} yield (s, e, sn.geom distanceSphere en.geom)  
        } 
        
        val roads = for {
        	w <- osmTables.ways 
        	    if w.tags ?& "highway"
        	    if w.tags +> "highway" =!= "path"
        	    if w.tags +> "highway" =!= "cycleway"
        	    if w.tags +> "highway" =!= "footway"
        	    if w.tags +> "highway" =!= "bridleway"
        	    if w.tags +> "highway" =!= "steps"
        	    if w.tags +> "highway" =!= "pedestrian"
        	    if w.tags +> "highway" =!= "proposed"
        } yield w
	
        val roadEdgeJoin = for {
        	c <- osmTables.wayNodes
        	n <- osmTables.wayNodes if n.sequenceId === c.sequenceId + 1 && n.wayId === c.wayId
        	r <- roads if r.id === c.wayId
        } yield (c, n, r)
	
        val roadNetBare = for {
        	(c, n, r) <- roadEdgeJoin
        	if !(r.tags ?& "oneway") || r.tags +> "oneway" =!= "-1"
        } yield (c.nodeId, n.nodeId)
	
        val roadNetBareBack = for {
        	(c, n, r) <- roadEdgeJoin
        	if (!(r.tags ?& "oneway") 
        			&& r.tags +> "highway" =!= "motorway" 
        			&& r.tags +> "highway" =!= "motorway_link"
        			&& (!(r.tags ?& "junction") ||  r.tags +> "junction" =!= "roundabout")) ||
	            r.tags +> "oneway" === "-1" ||
	            r.tags +> "oneway" === "no"
        } yield (n.nodeId, c.nodeId)
        
        val roadNetBareAll = roadNetBare union roadNetBareBack
        
        def execute() {
            database.withTransaction{ implicit s:Session =>
                import roadNetTables._ 
                roadNet.insert(roadNetQuery)
                roadNetUndir.insert(Query(roadNet))
                val todel = for {
                    r <- roadNetUndir
                    if Query(roadNetUndir).filter(r2 => r2.start === r.end && r2.end === r.start).exists
                    if r.start < r.end
                } yield r
                todel.delete
                roadNetUndir.insert(roadNetUndir.map(x => (x.end, x.start, x.cost)))
                val starts = roadNet.groupBy(_.start).map(x => x._1 -> 0)
                val ends = roadNet.groupBy(_.end).map(x => x._1 -> 0)
                roadNodes.insert(starts union ends)
            }
        }
    }
}