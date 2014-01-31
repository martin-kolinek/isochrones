package org.isochrone.hh

import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.RoadNetTableParsingComponent
import org.isochrone.ArgumentParser

class NodeNeighbourhoods(name: String) extends Table[(Long, Double)](name) {
    def nodeId = column[Long]("node_id")
    def neighbourhood = column[Double]("neighbourhood")
    def * = nodeId ~ neighbourhood
}

trait HHTables {
    val neighbourhoods: NodeNeighbourhoods
}

trait HHTableComponent {
    val hhTables: HHTables
}

class DefaultHHTablesWithPrefix(prefix: String) extends HHTables {
    val neighbourhoods = new NodeNeighbourhoods(prefix + "hh_node_neighbourhoods")
}

trait ConfigHHTableComponent extends HHTableComponent with RoadNetTableParsingComponent {
    self: ArgumentParser =>

    val hhTables = new DefaultHHTablesWithPrefix(roadNetPrefixLens.get(parsedConfig))
}