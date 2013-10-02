package org.isochrone.osm

import org.isochrone.util.db.MyPostgresDriver.simple._
import slick.jdbc.StaticQuery.interpolation
import org.isochrone.db.OsmTableComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.ActionComponent
import org.isochrone.ActionComponent
import org.isochrone.db.EdgeTable

trait RoadImporterComponent {
    self: OsmTableComponent with RoadNetTableComponent with DatabaseProvider =>

    object roadImporter {
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
                    && (!(r.tags ?& "junction") || r.tags +> "junction" =!= "roundabout")) ||
                    r.tags +> "oneway" === "-1" ||
                    r.tags +> "oneway" === "no"
            } yield (n.nodeId, c.nodeId)

            val roadNetBareAll = roadNetBare union roadNetBareBack

            for {
                (s, e) <- roadNetBareAll
                sn <- osmTables.nodes if sn.id === s
                en <- osmTables.nodes if en.id === e
            } yield (s, e, (sn.geom distanceSphere en.geom).asColumnOf[Double])
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
                && (!(r.tags ?& "junction") || r.tags +> "junction" =!= "roundabout")) ||
                r.tags +> "oneway" === "-1" ||
                r.tags +> "oneway" === "no"
        } yield (n.nodeId, c.nodeId)

        val roadNetBareAll = roadNetBare union roadNetBareBack

        def execute() {
            database.withTransaction { implicit s: Session =>
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
                sqlu"""INSERT INTO #${roadNetTables.roadNodes.tableName}(id, region, geom)
                SELECT DISTINCT n.id, 0, n.geom FROM #${roadNetTables.roadNet.tableName} rn 
                    inner join #${osmTables.nodes.tableName} n on rn.start_node = n.id OR rn.end_node = n.id
                """.execute()
                roadRegions.insert(0 -> Double.PositiveInfinity)
            }
        }
    }
}