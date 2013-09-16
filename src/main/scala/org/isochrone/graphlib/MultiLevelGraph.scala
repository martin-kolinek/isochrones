package org.isochrone.graphlib

trait GraphWithRegionsType[Node, Region] extends GraphType[Node] {
    def nodeRegion(nd:Node):Option[Region]
    
    def nodeEccentricity(nd:Node):Double
    
    def singleRegion(rg:Region) = {
        new GraphType[Node] {
            
        }
}
}

trait MultiLevelGraphComponent {
    type NodeType
    type RegionType <: Region

    val levels: Seq[GraphWithRegionsType[NodeType, RegionType]]
}