package org.isochrone.partition.merging

import org.scalatest.FunSuite
import org.isochrone.simplegraph.SimpleGraph
import org.isochrone.graphlib._

class AlgorithmTest extends FunSuite {
	/*test("merging algorithm works with controlled functions") {
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
		
		def cost(p:Partition[Int]) = p.cellNeighbours.keys.count(_.size==2)
		
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
	}*/
	
	test("Sanity checks for step function") {
		/*val directed = Seq(
				1->2, 1->3, 1->4, 2->3, 2->4, 
				4->5,
				5->6, 5->7, 5->8, 6->7, 6->8)
		val undirected = directed ++ directed.map(_.swap)
		val graph = new SimpleGraph(undirected.map(x=>(x._1, x._2, 1.0)):_*)*/
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
		implicit val gl = graph.graphlib
		val part = Partition(Seq(1, 2, 3, 4, 5, 6, 7, 8), FunctionLibrary.mergePriority[Int] _)
        for(_ <- 1 to 100) {
            part.step()
            checkCellNeighbours(part)
        }
	}

    def checkCellNeighbours[T:HasNeighbours](part:Partition[T]) {
        val nodesToCells = part.cells.flatMap(x=>x.nodes.map(y=>x -> y)).map(_.swap).toMap
        val cellNeighbours = part.cells.map(x=> x -> x.nodes.flatMap(_.neighbours).map(_._1).map(nodesToCells).toSet).toMap
        assert(cellNeighbours == part.cellNeighbours)
    }

    def checkPriorities[T:HasNeighbours](part:Partition[T]) {
        val should = for {
            c <- part.cells
            c2 <- part.cellNeighbours(c)
        } yield (Set(c, c2), part.mergePriority(c, c2))

        def ok(p1:(scala.collection.Set[Cell[T]], Double), p2:(scala.collection.Set[Cell[T]], Double)) = p1._1 == p2._1 && math.abs(p1._2 - p2._2)<0.0001

        assert(should.forall(p=>part.priorities.exists(ok(p, _))))
        assert(part.priorities.forall(p=>should.exists(ok(p, _))))
    }

    def checkBoundaryEdges[T:HasNeighbours](part:Partition[T]) {
        val nodesToCells = part.cells.flatMap(x=>x.nodes.map(y=>x -> y)).map(_.swap).toMap
        val neighs = for {
            cell <- part.cells
            node <- cell.nodes
            (neigh, _) <- node.neighbours
        } yield Set(cell, nodesToCells(neigh)) -> 1
        val should = neighs.groupBy(_._1).map(x=>x._1->x._2.map(_._2).sum)
        assert(should == part.boundaryEdges)
        assert(should.map(_._2).sum == part.boundaryEdgeCount)
    }
}
