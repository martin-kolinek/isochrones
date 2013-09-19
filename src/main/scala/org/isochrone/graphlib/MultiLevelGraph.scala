package org.isochrone.graphlib

trait GraphWithRegionsType[Node, Region] extends GraphType[Node] {
    self =>
    def nodeRegion(nd: Node): Option[Region]

    def nodeEccentricity(nd: Node): Double

    def singleRegion(rg: Region) = {
        new GraphType[Node] {
            def neighbours(nd: Node) = self.neighbours(nd).filter(x => self.nodeRegion(x._1) == Some(rg))
            def nodes = self.nodes.filter(x => self.nodeRegion(x) == Some(rg))
        }
    }
}

trait GraphWithRegionsComponent extends GraphComponent {
    type RegionType <: Region
    val graph: GraphWithRegionsType[NodeType, RegionType]
}

trait MultiLevelGraphComponent {
    type NodeType
    type RegionType <: Region

    val levels: Seq[GraphWithRegionsType[NodeType, RegionType]]
}