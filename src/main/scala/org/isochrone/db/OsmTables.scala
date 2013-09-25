package org.isochrone.db

import org.isochrone.util.db.MyPostgresDriver.simple._
import com.vividsolutions.jts.geom.Geometry
import java.sql.Timestamp

trait OsmTableComponent {
    class Ways extends Table[(Long, Map[String, String], Geometry)]("ways") {
        def id = column[Long]("id")
        def tags = column[Map[String, String]]("tags")
        def linestring = column[Geometry]("linestring")
        def * = id ~ tags ~ linestring
    }

    class WayNodes extends Table[(Long, Long, Int)]("way_nodes") {
        def wayId = column[Long]("way_id")
        def nodeId = column[Long]("node_id")
        def sequenceId = column[Int]("sequence_id")
        def * = wayId ~ nodeId ~ sequenceId
    }

    class Nodes extends Table[(Long, Geometry, Int, Int, Timestamp, Long, Map[String, String])]("nodes") {
        def id = column[Long]("id")
        def geom = column[Geometry]("geom")
        def version = column[Int]("version")
        def userId = column[Int]("user_id")
        def timestamp = column[Timestamp]("tstamp")
        def changesetId = column[Long]("changeset_id")
        def tags = column[Map[String, String]]("tags")
        def * = id ~ geom ~ version ~ userId ~ timestamp ~ changesetId ~ tags
    }

    object osmTables {
        val ways: Ways = new Ways
        val wayNodes: WayNodes = new WayNodes
        val nodes: Nodes = new Nodes
    }
}
