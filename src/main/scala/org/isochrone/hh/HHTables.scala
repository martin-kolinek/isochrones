package org.isochrone.hh

import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.db.RoadNetTableParsingComponent
import org.isochrone.ArgumentParser
import org.isochrone.db.EdgeTable
import org.isochrone.db.HigherRoadNetTableParsingComponent
import org.isochrone.OptionParserComponent
import shapeless.Lens
import scopt.OptionParser
import org.isochrone.db.MultiLevelRoadNetTableParsingComponent

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
    val reverseShortcutEdges: TableQuery[EdgeTable]
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
    val reverseShortcutEdges = TableQuery(t => new EdgeTable(t, prefix + "hh_rev_shortcuts"))
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

trait ConfigHigherHHTableComponent extends HigherHHTableComponent with HigherRoadNetTableParsingComponent {
    self: ArgumentParser =>
    val higherHHTables = new DefaultHHTablesWithPrefix(higherRoadNetPrefixLens.get(parsedConfig))
}

trait MultiLevelHHTableComponent {
    val hhTableLevels: IndexedSeq[HHTables]
}

trait ConfigMultiLevelHHTableComponent extends MultiLevelHHTableComponent with MultiLevelRoadNetTableParsingComponent with OptionParserComponent {
    self: ArgumentParser =>

    val hhTableLevels = multiLevelRoadNetPrefixLens.get(parsedConfig).map(new DefaultHHTablesWithPrefix(_)).toIndexedSeq

}