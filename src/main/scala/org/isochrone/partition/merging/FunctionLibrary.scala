package org.isochrone.partition.merging

import org.isochrone.graphlib._
import scala.math
import scala.util.Random

trait MergingAlgorithmPropertiesComponent {
    self: CellComponent with PartitionComponent =>

    trait MergingAlgorithmProperties {
        def mergePriorityFunc(c1: Cell, c2: Cell): Double
        def partitionValueFunc(p: Partition): Double
    }

    val mergeAlgProps: MergingAlgorithmProperties
}

trait FunctionLibraryComponent {
    self: CellComponent with PartitionComponent =>

    object FunctionLibrary {

        def mergePriority(c1: Cell, c2: Cell) = {
            val connecting = c1.leaving.toSeq.map(_._2).filter(c2.nodes.contains(_)).size
            val newBoundary = (c1 ++ c2).boundarySize
            val rand = 1.0
            connecting * (1.0 + c1.boundarySize + c2.boundarySize - newBoundary) / (c1.size * c2.size)
        }

        def randMergePriority(c1: Cell, c2: Cell) = {
            (Random.nextDouble / 100.0 + 1.0) * mergePriority(c1, c2)
        }

        def boundaryEdgesCellSize[T](size: Int) = (p: Partition) => {
            cellSize(size)(p) - p.boundaryEdgeCount.toDouble
        }

        def cellSize[T](size: Int) = (p: Partition) => -p.cells.toSeq.map { x =>
            val rat = x.size.toDouble / size
            (rat * rat + 1.0) / rat - 2.0
        }.map(x => x * x).sum
    }
}

trait DefaultMergingAlgPropertiesComponent extends MergingAlgorithmPropertiesComponent with FunctionLibraryComponent {
    self: CellComponent with PartitionComponent =>
        
    val mergeAlgProps = new MergingAlgorithmProperties {
        def mergePriorityFunc(c1: Cell, c2: Cell): Double = FunctionLibrary.mergePriority(c1, c2)
        def partitionValueFunc(p: Partition): Double = FunctionLibrary.boundaryEdgesCellSize(70)(p)
    }
}
