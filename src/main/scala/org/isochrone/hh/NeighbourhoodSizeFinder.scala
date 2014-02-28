package org.isochrone.hh

import org.isochrone.dijkstra.DijkstraAlgorithmComponent
import org.isochrone.graphlib.GraphComponentBase
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.dijkstra.DijkstraAlgorithmProviderComponent
import org.isochrone.graphlib.GraphType
import org.isochrone.OptionParserComponent
import scopt.OptionParser
import org.isochrone.ArgumentParser

trait NeighbourhoodSizeFinderComponent extends GraphComponentBase {
    self: DijkstraAlgorithmProviderComponent with NeighbourhoodCountComponent =>

    type NodeType = Long

    def neighSizeFinder(g: GraphType[NodeType]) = new NeighbourhoodSizeFinder(dijkstraForGraph(g))

    class NeighbourhoodSizeFinder(dijk: DijkstraAlgorithmClass) {
        def findNeighbourhoodSize(nd: NodeType, count: Int) = {
            dijk.helper.compute(nd).view.drop(count - 1).head._2
        }

        def saveNeighbourhoodSize(nd: NodeType, result: NodeNeighbourhoods)(implicit s: Session) = {
            result.insert(nd -> findNeighbourhoodSize(nd, neighbourhoodCount))
        }
    }
}

trait NeighbourhoodCountComponent {
    def neighbourhoodCount: Int
}

trait ConfigNeighbourhoodCountComponent extends OptionParserComponent with NeighbourhoodCountComponent {
    self: ArgumentParser =>
    lazy val neighCountLens = registerConfig(20)

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[Int]("neighbourhood-count").text("The size of highway hierarchies neighbourhoods (default = 20)").action((x, c) => neighCountLens.set(c)(x))
    }

    lazy val neighbourhoodCount = neighCountLens.get(parsedConfig)
}
