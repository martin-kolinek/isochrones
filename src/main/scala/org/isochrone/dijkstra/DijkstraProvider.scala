package org.isochrone.dijkstra

import org.isochrone.graphlib.GraphComponent
import org.isochrone.graphlib.GraphType

trait DijkstraProvider {
	def dijkstraForGraph[Node](grp:GraphType[Node]): DijkstraAlgorithmComponent with GraphComponent {
	    type NodeType = Node
	}
}

trait DefaultDijkstraProvider extends DijkstraProvider {
	def dijkstraForGraph[Node](grp:GraphType[Node]) = new DijkstraAlgorithmComponent with GraphComponent {
	    type NodeType = Node
	    val graph = grp
	}
}