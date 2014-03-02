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

class DescendLimits(name: String) extends Table[(Long, Double)](name) {
    def nodeId = column[Long]("node_id")
    def descendLimit = column[Double]("descend")
    def * = nodeId ~ descendLimit
}

trait HHTables {
    val neighbourhoods: NodeNeighbourhoods
    val reverseNeighbourhoods: NodeNeighbourhoods
    val shortcutEdges: EdgeTable
    val descendLimit: DescendLimits
}

trait HHTableComponent {
    val hhTables: HHTables
}

class DefaultHHTablesWithPrefix(prefix: String) extends HHTables {
    val neighbourhoods = new NodeNeighbourhoods(prefix + "hh_node_neighbourhoods")
    val reverseNeighbourhoods = new NodeNeighbourhoods(prefix + "hh_node_rev_neighbourhoods")
    val shortcutEdges = new EdgeTable(prefix + "hh_shortcuts")
    val descendLimit = new DescendLimits(prefix + "hh_descend")
}

trait ConfigHHTableComponent extends HHTableComponent with RoadNetTableParsingComponent {
    self: ArgumentParser =>

    val hhTables = new DefaultHHTablesWithPrefix(roadNetPrefixLens.get(parsedConfig))
}

trait HigherHHTableComponent {
    val higherHHTables: HHTables
}

trait ConfigHigherHHTableComponent extends HigherHHTableComponent with RoadNetTableParsingComponent {
    self: ArgumentParser =>
    val higherHHTables = new DefaultHHTablesWithPrefix(roadNetPrefixLens.get(parsedConfig))
}
