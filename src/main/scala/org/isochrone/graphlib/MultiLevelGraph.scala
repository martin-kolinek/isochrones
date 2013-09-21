package org.isochrone.graphlib

trait GraphWithRegionsType[Node, Region] extends GraphType[Node] {
    self =>
    def nodeRegion(nd: Node): Option[Region]

    def nodeEccentricity(nd: Node): Double

    def singleRegion(rg: Region) = filterRegions(_ == rg)

    def filterRegions(func: Region => Boolean) = new GraphType[Node] {
        def neighbours(nd: Node) = self.neighbours(nd).filter(x => self.nodeRegion(x._1).filter(func).isDefined)
        def nodes = self.nodes.filter(x => self.nodeRegion(x).map(func).isDefined)
    }
}

trait GraphWithRegionsComponentBase extends GraphComponentBase {
    type RegionType <: Region
}

trait GraphWithRegionsComponent extends GraphComponent with GraphWithRegionsComponentBase {
    val graph: GraphWithRegionsType[NodeType, RegionType]
}

trait MultiLevelGraphComponent extends GraphWithRegionsComponentBase {
    val levels: Seq[GraphWithRegionsType[NodeType, RegionType]]
}