package org.isochrone.partition.merging

import org.scalatest.FunSuite
import org.isochrone.simplegraph.SimpleGraph
import org.isochrone.graphlib._
import org.isochrone.util.RandomGraph

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
		
		val graph = SimpleGraph(unweighted.map(x=>(x._1, x._2, 1.0)):_*)
		def priority(c1:Cell[Int], c2:Cell[Int]) = {
			val merged = c1.nodes ++ c2.nodes
			if(merged == Set(1, 2) || merged == Set(3, 4)) 
				1.0 
			else 
				0.0
		}
		
		def cost(p:Partition[Int]) = p.cellNeighbours.keys.count(_.size==2)
		
		implicit val gl = graph.instance
		
		assert(partition(Seq(1, 2, 3, 4), priority, cost) == Set(Set(1, 2), Set(3, 4)))
	}
	
	test("Merging algorithm with bridge") {
		val directed = Seq(
				1->2, 1->3, 1->4, 2->3, 2->4, 
				4->5,
				5->6, 5->7, 5->8, 6->7, 6->8)
		val undirected = directed ++ directed.map(_.swap)
		val graph = SimpleGraph(undirected.map(x=>(x._1, x._2, 1.0)):_*)
		implicit val gl = graph.instance
		info(partition(Seq(1, 2, 3, 4, 5, 6, 7, 8), 
				FunctionLibrary.mergePriority[Int] _, 
				FunctionLibrary.boundaryEdgesCellSize[Int](4)).toString)
	}
	
	test("Sanity checks for step function on random graphs") {
		
		for (i <- 1 to 3) {
			val graph = RandomGraph.randomSymmetricGraph(50, 150)
			implicit val gl = graph.instance
			val comutative = Seq(
				(x:Cell[Int], y:Cell[Int])=>1.0+x.size+y.size,
				FunctionLibrary.mergePriority[Int] _)
			for (mergeFunc <- comutative) {
				//info(graph.toString)
				val part = Partition(graph.nodes.toSeq, mergeFunc)
				for(i <- 1 to 70) {
					//info(i.toString)
					checkCellNeighbours(part)
					checkPriorities(part, true)
					checkBoundaryEdges(part)
					checkCells(part)
					/*if(!part.priorities.empty)
				    info(part.priorities.maximum.toString)*/
					part.step()
				}
			}
		}
	}

    def checkCellNeighbours[T:HasNeighbours](part:Partition[T]) {
        val nodesToCells = part.cells.flatMap(x=>x.nodes.map(y=>x -> y)).map(_.swap).toMap
        val cellNeighbours = part.cells.map(x=> x -> x.nodes.flatMap(_.neighbours).map(_._1).map(nodesToCells).filter(y=>y!=x).toSet).filter(!_._2.isEmpty).toMap
        if(cellNeighbours!=part.cellNeighbours) {
        	info("Cell neighbours do not match")
        	info(cellNeighbours.toString)
        	info(part.cellNeighbours.toString)
        }
        assert(cellNeighbours == part.cellNeighbours)
    }

    def checkPriorities[T:HasNeighbours](part:Partition[T], checkComutative:Boolean) {
        val should = for {
            c <- part.cells
            c2 <- part.cellNeighbours.getOrElse(c, Set())
        } yield (Set(c, c2), part.mergePriority(c, c2))

        if(checkComutative) {
        	for {
        		c <- part.cells
        		c2 <- part.cellNeighbours.getOrElse(c, Set())
        	} assert(scala.math.abs(part.mergePriority(c, c2)-part.mergePriority(c2, c))<0.00001)
        }
        
        def ok(p1:(scala.collection.Set[Cell[T]], Double), p2:(scala.collection.Set[Cell[T]], Double)) = p1._1 == p2._1 && math.abs(p1._2 - p2._2)<0.0001
        if(!should.forall(p=>part.priorities.exists(ok(p, _))) || !part.priorities.forall(p=>should.exists(ok(p, _)))) {
        	info("Priorities do not match")
        	info(should.toString)
        	info(part.priorities.toString)
        }
        assert(should.forall(p=>part.priorities.exists(ok(p, _))))
        assert(part.priorities.forall(p=>should.exists(ok(p, _))))
    }

    def checkBoundaryEdges[T:HasNeighbours](part:Partition[T]) {
        val nodesToCells = part.cells.flatMap(x=>x.nodes.map(y=>x -> y)).map(_.swap).toMap
        val neighs = for {
            cell <- part.cells.toSeq
            node <- cell.nodes.toSeq
            (neigh, _) <- node.neighbours.toSeq
            neighCell = nodesToCells(neigh)
            if neighCell != cell
        } yield Set(cell, neighCell) -> 1
        val should = neighs.groupBy(_._1).map(x=>x._1->x._2.map(_._2).sum/2)
        if(should != part.boundaryEdges) {
        	info("boundary edges do not match")
        	info(should.toString)
        	info(part.boundaryEdges.toString)
        }
        assert(should == part.boundaryEdges)
        if(should.map(_._2).sum != part.boundaryEdgeCount) {
        	info("boundary edge counts do not match")
        	info(should.map(_._2).sum.toString)
        	info(part.boundaryEdgeCount.toString)
        }
        assert(should.map(_._2).sum == part.boundaryEdgeCount)
    }
    
    def checkCells[T:HasNeighbours](part:Partition[T]) {
    	for (cell<-part.cells) {
    		val should = cell.nodes.flatMap(
    				x=>x.neighbours.filter(y=> !cell.nodes.contains(y._1)).map(y=>x -> y._1)).toSet
    		assert(should == cell.leaving)
    	}
    }
}
