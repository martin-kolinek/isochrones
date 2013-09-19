package org.isochrone.partition
import org.isochrone.graphlib._
import org.isochrone.dijkstra.DijkstraAlgorithmComponent

trait RegionAnalyzerComponent {
    self: DijkstraAlgorithmComponent with GraphComponent =>
    object RegionAnalyzer {
        def borderNodeDistances(borderNodes: Set[NodeType]) = {
            val res = for {
                n <- borderNodes
                dijk = DijkstraAlgorithm.compute(Seq(n -> 0.0))
            } yield n -> dijk.filter(x => borderNodes.contains(x._1) && x._1 != n)
            res.toSeq
        }
    }
}

trait RegionAnalyzerProviderComponent {
    trait RegionAnalyzerProvider {
        def getAnalyzer[Node](graph:GraphType[Node]):RegionAnalyzerComponent with GraphComponent {
            type NodeType = Node
        }
    }
    
    val regionAnalyzerProvider:RegionAnalyzerProvider
}