package org.isochrone.db

import org.isochrone.util.db.MyPostgresDriver.simple._
import com.vividsolutions.jts.geom.Geometry
import java.sql.Timestamp

trait OsmTableComponent {
    class Ways(tag: Tag) extends Table[(Long, Map[String, String], Geometry)](tag, "ways") {
        def id = column[Long]("id")
        def tags = column[Map[String, String]]("tags")
        def linestring = column[Geometry]("linestring")
        def * = (id, tags, linestring)
    }

    class WayNodes(tag: Tag) extends Table[(Long, Long, Int)](tag, "way_nodes") {
        def wayId = column[Long]("way_id")
        def nodeId = column[Long]("node_id")
        def sequenceId = column[Int]("sequence_id")
        def * = (wayId, nodeId, sequenceId)
    }

    class Nodes(tag: Tag) extends Table[(Long, Geometry, Int, Int, Long, Map[String, String])](tag, "nodes") {
        def id = column[Long]("id")
        def geom = column[Geometry]("geom")
        def version = column[Int]("version")
        def userId = column[Int]("user_id")
        def changesetId = column[Long]("changeset_id")
        def tags = column[Map[String, String]]("tags")
        def * = (id, geom, version, userId, changesetId, tags)
    }

    object osmTables {
        val ways: TableQuery[Ways] = TableQuery[Ways]
        val wayNodes: TableQuery[WayNodes] = TableQuery[WayNodes]
        val nodes: TableQuery[Nodes] = TableQuery[Nodes]
    }
}
