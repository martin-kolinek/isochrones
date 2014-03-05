package org.isochrone.hh

import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.RoadNetTableParsingComponent
import org.isochrone.ArgumentParser
import org.isochrone.db.EdgeTable

class NodeNeighbourhoods(tag: Tag, name: String) extends Table[(Long, Double)](tag, name) {
    def nodeId = column[Long]("node_id")
    def neighbourhood = column[Double]("neighbourhood")
    def * = (nodeId, neighbourhood)
}

class DescendLimits(tag: Tag, name: String) extends Table[(Long, Double)](tag, name) {
    def nodeId = column[Long]("node_id")
    def descendLimit = column[Double]("descend")
    def * = (nodeId, descendLimit)
}

trait HHTables {
    val neighbourhoods: TableQuery[NodeNeighbourhoods]
    val reverseNeighbourhoods: TableQuery[NodeNeighbourhoods]
    val shortcutEdges: TableQuery[EdgeTable]
    val descendLimit: TableQuery[DescendLimits]
    val shortcutReverseLimit: TableQuery[DescendLimits]
}

trait HHTableComponent {
    val hhTables: HHTables
}

class DefaultHHTablesWithPrefix(prefix: String) extends HHTables {
    val neighbourhoods = TableQuery(t => new NodeNeighbourhoods(t, prefix + "hh_node_neighbourhoods"))
    val reverseNeighbourhoods = TableQuery(t => new NodeNeighbourhoods(t, prefix + "hh_node_rev_neighbourhoods"))
    val shortcutEdges = TableQuery(t => new EdgeTable(t, prefix + "hh_shortcuts"))
    val descendLimit = TableQuery(t => new DescendLimits(t, prefix + "hh_descend"))
    val shortcutReverseLimit = TableQuery(t => new DescendLimits(t, prefix + "hh_shortcut_rev"))
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
