package org.isochrone.partition.merging

import org.scalatest.FunSuite
import org.isochrone.simplegraph.SimpleGraph

class AlgorithmTest extends FunSuite {
	test("merging algorithm works with controlled functions") {
		val unweighted = Seq(
				1->2,
				2->1,
				1->3,
				3->1,
				2->3,
				3->2,
				3->4,
				4->3)
		
		val graph = new SimpleGraph(unweighted.map(x=>(x._1, x._2, 1.0)):_*)
		def priority(c1:Cell[Int], c2:Cell[Int]) = {
			val merged = c1.nodes ++ c2.nodes
			if(merged == Set(1, 2) || merged == Set(3, 4)) 
				1.0 
			else 
				0.0
		}
		
		def cost(p:Partition[Int]) = p.cells.count(_.size==2)
		
		implicit val gl = graph.graphlib
		
		assert(partition(Seq(1, 2, 3, 4), priority, cost) == Set(Set(1, 2), Set(3, 4)))
	}
	
	test("Merging algorithm with bridge") {
		val directed = Seq(
				1->2, 1->3, 1->4, 2->3, 2->4, 
				4->5,
				5->6, 5->7, 5->8, 6->7, 6->8)
		val undirected = directed ++ directed.map(_.swap)
		val graph = new SimpleGraph(undirected.map(x=>(x._1, x._2, 1.0)):_*)
		implicit val gl = graph.graphlib
		info(partition(Seq(1, 2, 3, 4, 5, 6, 7, 8), 
				FunctionLibrary.mergePriority[Int] _, 
				FunctionLibrary.negAvgSearchGraphSize[Int] _).toString)
	}
}