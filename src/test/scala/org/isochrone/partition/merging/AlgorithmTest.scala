package org.isochrone.partition.merging

import org.scalatest.FunSuite
import org.isochrone.graphlib._
import org.isochrone.simplegraph.SimpleGraphComponent
import org.isochrone.dijkstra.DefaultDijkstraProvider
import org.isochrone.simplegraph.SimpleGraphComponent
import org.isochrone.dijkstra.DefaultDijkstraProvider
import org.isochrone.util.RandomGraphComponent
import org.isochrone.dijkstra.DefaultDijkstraProvider
import org.isochrone.dijkstra.DefaultDijkstraProvider

class AlgorithmTest extends FunSuite {
    test("merging algorithm works with controlled functions") {
        new SimpleGraphComponent with GraphComponent with MergingPartitionerComponent with MergingAlgorithmPropertiesComponent with DefaultDijkstraProvider {
            type NodeType = Int
            val mergeAlgProps = new MergingAlgorithmProperties {
                def mergePriorityFunc(c1: Cell, c2: Cell) = {
                    val merged = c1.nodes ++ c2.nodes
                    if (merged == Set(1, 2) || merged == Set(3, 4))
                        1.0
                    else
                        0.0
                }
                def partitionValueFunc(p: Partition) = p.cellNeighbours.keys.count(_.size == 2)
            }

            val unweighted = Seq(
                1 -> 2,
                2 -> 1,
                1 -> 3,
                3 -> 1,
                2 -> 3,
                3 -> 2,
                3 -> 4,
                4 -> 3)

            val graph = SimpleGraph(unweighted.map(x => (x._1, x._2, 1.0)): _*)

            assert(MergingPartitioner.partition() == Set(Set(1, 2), Set(3, 4)))
        }
    }

    test("Merging algorithm works with empty graph") {
        new SimpleGraphComponent with MergingPartitionerComponent with GraphComponent with DefaultDijkstraProvider with DefaultMergingAlgPropertiesComponent {
            type NodeType = Int
            val graph = SimpleGraph()
            assert(MergingPartitioner.partition() == Set())
        }
    }

    test("Merging algorithm with bridge") {
        new SimpleGraphComponent with MergingPartitionerComponent with GraphComponent with MergingAlgorithmPropertiesComponent with DefaultDijkstraProvider with FunctionLibraryComponent {
            type NodeType = Int
            val mergeAlgProps = new MergingAlgorithmProperties {
                def mergePriorityFunc(c1: Cell, c2: Cell) = FunctionLibrary.mergePriority(c1, c2)
                def partitionValueFunc(p: Partition) = FunctionLibrary.boundaryEdgesCellSize(4)(p)
            }

            val directed = Seq(
                1 -> 2, 1 -> 3, 1 -> 4, 2 -> 3, 2 -> 4,
                4 -> 5,
                5 -> 6, 5 -> 7, 5 -> 8, 6 -> 7, 6 -> 8)
            val undirected = directed ++ directed.map(_.swap)
            val graph = SimpleGraph(undirected.map(x => (x._1, x._2, 1.0)): _*)
            info(MergingPartitioner.partition().toString)
        }
    }

    test("Sanity checks for step function on random graphs") {
        for (i <- 1 to 3; funcn <- 0 to 1) {
            new RandomGraphComponent with DefaultDijkstraProvider with PartitionComponent with GraphComponent with CellComponent with MergingAlgorithmPropertiesComponent with FunctionLibraryComponent {
                def checkCellNeighbours(part: Partition) {
                    val nodesToCells = part.cells.flatMap(x => x.nodes.map(y => x -> y)).map(_.swap).toMap
                    val cellNeighbours = part.cells.map(x => x -> x.nodes.flatMap(graph.neighbours _).map(_._1).map(nodesToCells).filter(y => y != x).toSet).filter(!_._2.isEmpty).toMap
                    if (cellNeighbours != part.cellNeighbours) {
                        info("Cell neighbours do not match")
                        info(cellNeighbours.toString)
                        info(part.cellNeighbours.toString)
                    }
                    assert(cellNeighbours == part.cellNeighbours)
                }

                def checkPriorities(part: Partition, checkComutative: Boolean) {
                    val should = for {
                        c <- part.cells
                        c2 <- part.cellNeighbours.getOrElse(c, Set())
                    } yield (Set(c, c2), mergeAlgProps.mergePriorityFunc(c, c2))

                    if (checkComutative) {
                        for {
                            c <- part.cells
                            c2 <- part.cellNeighbours.getOrElse(c, Set())
                        } assert(scala.math.abs(mergeAlgProps.mergePriorityFunc(c, c2) - mergeAlgProps.mergePriorityFunc(c2, c)) < 0.00001)
                    }

                    def ok(p1: (scala.collection.Set[Cell], Double), p2: (scala.collection.Set[Cell], Double)) = p1._1 == p2._1 && math.abs(p1._2 - p2._2) < 0.0001
                    if (!should.forall(p => part.priorities.exists(ok(p, _))) || !part.priorities.forall(p => should.exists(ok(p, _)))) {
                        info("Priorities do not match")
                        info(should.toString)
                        info(part.priorities.toString)
                    }
                    assert(should.forall(p => part.priorities.exists(ok(p, _))))
                    assert(part.priorities.forall(p => should.exists(ok(p, _))))
                }

                def checkBoundaryEdges(part: Partition) {
                    val nodesToCells = part.cells.flatMap(x => x.nodes.map(y => x -> y)).map(_.swap).toMap
                    val neighs = for {
                        cell <- part.cells.toSeq
                        node <- cell.nodes.toSeq
                        (neigh, _) <- graph.neighbours(node).toSeq
                        neighCell = nodesToCells(neigh)
                        if neighCell != cell
                    } yield Set(cell, neighCell) -> 1
                    val should = neighs.groupBy(_._1).map(x => x._1 -> x._2.map(_._2).sum / 2)
                    if (should != part.boundaryEdges) {
                        info("boundary edges do not match")
                        info(should.toString)
                        info(part.boundaryEdges.toString)
                    }
                    assert(should == part.boundaryEdges)
                    if (should.map(_._2).sum != part.boundaryEdgeCount) {
                        info("boundary edge counts do not match")
                        info(should.map(_._2).sum.toString)
                        info(part.boundaryEdgeCount.toString)
                    }
                    assert(should.map(_._2).sum == part.boundaryEdgeCount)
                }

                def checkCells(part: Partition) {
                    for (cell <- part.cells) {
                        val should = cell.nodes.flatMap(
                            x => graph.neighbours(x).toList.filter(y => !cell.nodes.contains(y._1)).map(y => x -> y._1)).toSet
                        assert(should == cell.leaving)
                    }
                }

                val graph = RandomGraph.randomSymmetricGraph(50, 150)
                val comutative = Seq(
                    (x: Cell, y: Cell) => 1.0 + x.size + y.size,
                    FunctionLibrary.mergePriority _)
                val mergeAlgProps = new MergingAlgorithmProperties {
                    def mergePriorityFunc(c1: Cell, c2: Cell) = comutative(funcn)(c1, c2)
                    def partitionValueFunc(p: Partition) = 0
                }

                val part = Partition(graph.nodes.toSeq)
                for (i <- 1 to 70) {

                    checkCellNeighbours(part)
                    checkPriorities(part, true)
                    checkBoundaryEdges(part)
                    checkCells(part)

                    part.step()
                }
            }
        }
    }

}
