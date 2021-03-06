package org.isochrone.graphlib

trait GraphWithRegionsType[Node, Region] extends GraphType[Node] {
    self =>
    def nodeRegion(nd: Node): Option[Region]

    def regionDiameter(rg: Region): Double

    def nodeEccentricity(nd: Node): Double

    def singleRegion(rg: Region) = filterRegions(_ == rg)

    def filterRegions(func: Region => Boolean) = new GraphType[Node] {
        def neighbours(nd: Node) = self.neighbours(nd).filter(x => self.nodeRegion(x._1).filter(func).isDefined)
        def nodes = self.nodes.filter(x => self.nodeRegion(x).filter(func).isDefined)
        def edgeCost(start: Node, end: Node) = {
            if (self.nodeRegion(start).filter(func).isDefined && self.nodeRegion(end).filter(func).isDefined)
                self.edgeCost(start, end)
            else
                None
        }
    }

    def regions: Traversable[Region]
}

trait GraphWithRegionsComponentBase extends GraphComponentBase {
    type RegionType
}

trait GraphWithRegionsComponent extends GraphComponent with GraphWithRegionsComponentBase {
    val graph: GraphWithRegionsType[NodeType, RegionType]
}

trait MultiLevelGraphComponent extends GraphWithRegionsComponentBase {
    val levels: Seq[GraphWithRegionsType[NodeType, RegionType]]
}
 