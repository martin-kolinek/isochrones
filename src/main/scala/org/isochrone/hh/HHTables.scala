package org.isochrone.hh

import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.RoadNetTableParsingComponent
import org.isochrone.ArgumentParser
import org.isochrone.db.EdgeTable

class NodeNeighbourhoods(name: String) extends Table[(Long, Double)](name) {
    def nodeId = column[Long]("node_id")
    def neighbourhood = column[Double]("neighbourhood")
    def * = nodeId ~ neighbourhood
}

trait HHTables {
    val neighbourhoods: NodeNeighbourhoods
    val reverseNeighbourhoods: NodeNeighbourhoods
    val shortcutEdges: EdgeTable
}

trait HHTableComponent {
    val hhTables: HHTables
}

class DefaultHHTablesWithPrefix(prefix: String) extends HHTables {
    val neighbourhoods = new NodeNeighbourhoods(prefix + "hh_node_neighbourhoods")
    val reverseNeighbourhoods = new NodeNeighbourhoods(prefix + "hh_node_rev_neighbourhoods")
    val shortcutEdges = new EdgeTable(prefix + "hh_shortcuts")
}

trait ConfigHHTableComponent extends HHTableComponent with RoadNetTableParsingComponent {
    self: ArgumentParser =>

    val hhTables = new DefaultHHTablesWithPrefix(roadNetPrefixLens.get(parsedConfig))
}