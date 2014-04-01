package org.isochrone.osm

import org.isochrone.util.db.MyPostgresDriver.simple._
import slick.jdbc.StaticQuery.interpolation
import org.isochrone.db.OsmTableComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.ActionComponent
import org.isochrone.ActionComponent
import org.isochrone.db.EdgeTable
import com.vividsolutions.jts.geom.Geometry
import com.typesafe.scalalogging.slf4j.Logging

trait RoadImporterComponent {
    self: OsmTableComponent with RoadNetTableComponent with DatabaseProvider with CostAssignerComponent =>

    object roadImporter extends Logging {
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
            } yield (s, e, getRoadCost(sn.geom, en.geom), false, sn.geom.shortestLine(en.geom).asColumnOf[Geometry])
        }
        logger.debug(s"roadNetQuery: ${roadNetQuery.selectStatement}")

        val backRoads = for {
            rn <- roadNetTables.roadNet
            if !roadNetTables.roadNet.filter(rn2 => rn2.start === rn.end && rn2.end === rn.start).exists
            sn <- roadNetTables.roadNodes if sn.id === rn.start
            en <- roadNetTables.roadNodes if en.id === rn.end
        } yield (rn.end, rn.start, getNoRoadCost(sn.geom, en.geom), true, en.geom.shortestLine(sn.geom).asColumnOf[Geometry])
        logger.debug(s"roadNetQuery: ${backRoads.selectStatement}")

        def execute() {
            database.withTransaction { implicit s: Session =>
                import roadNetTables._
                logger.debug("inserting roadNetQuery")
                roadNet.insert(roadNetQuery)
                logger.debug("inserting road_nodes")
                sqlu"""INSERT INTO #${roadNetTables.roadNodes.baseTableRow.tableName}(id, region, geom)
                SELECT DISTINCT n.id, 0, n.geom FROM #${roadNetTables.roadNet.baseTableRow.tableName} rn 
                    inner join #${osmTables.nodes.baseTableRow.tableName} n on rn.start_node = n.id OR rn.end_node = n.id
                """.execute()
                logger.debug("inserting backRoads")
                roadNet.insert(backRoads)
                logger.debug("inserting regions")
                roadRegions.insert(0 -> Double.PositiveInfinity)
            }
        }
    }
}