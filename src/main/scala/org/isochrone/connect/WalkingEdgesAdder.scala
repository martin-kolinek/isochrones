package org.isochrone.connect

import org.isochrone.db.DatabaseProvider
import scala.util.Random
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.dijkstra.DijkstraAlgorithmProviderComponent
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.util._
import org.isochrone.osm.SpeedCostAssignerComponent
import org.isochrone.dijkstra.DijkstraAlgorithmComponent
import org.isochrone.graphlib.GraphComponentBase
import com.vividsolutions.jts.geom.Geometry
import com.typesafe.scalalogging.slf4j.Logging
import org.isochrone.ArgumentParser
import org.isochrone.OptionParserComponent
import org.isochrone.ArgumentParser
import org.isochrone.graphlib.GraphType
import scopt.OptionParser
import slick.jdbc.StaticQuery.interpolation
import org.isochrone.db.RegularPartitionComponent
import org.isochrone.simplegraph.SimpleGraphComponent
import org.isochrone.graphlib.UnionGraph
import org.isochrone.db.SingleSessionProvider
import org.isochrone.db.SessionProviderComponent
import org.isochrone.dbgraph.DatabaseGraph
import scala.collection.mutable.HashSet
import scala.annotation.tailrec
import org.isochrone.visualize.ApproxEquidistAzimuthProjComponent
import org.isochrone.dijkstra.DijkstraAlgorithmClass

trait WalkingEdgesAdderComponent {
    def addWalkingEdges()
}

trait SimpleWalkingEdgesAdderComponent extends WalkingEdgesAdderComponent with GraphComponentBase with Logging with SimpleGraphComponent with WalkingEdgeFilter with ApproxEquidistAzimuthProjComponent {
    self: DatabaseProvider with RoadNetTableComponent with SpeedCostAssignerComponent with MaxCostQuotientComponent with RegularPartitionComponent with DijkstraAlgorithmProviderComponent =>
    override type NodeType = Long
    def addWalkingEdges() {
        regularPartition.regions.zipWithIndex.foreach((processRegion _).tupled)
    }

    def processRegion(bbox: regularPartition.BoundingBox, idx: Int) = {
        database.withTransaction { implicit s: Session =>
            logger.info(s"Processing region $bbox ($idx/${regularPartition.regionCount})")
            val gr = new DatabaseGraph(roadNetTables, 1000, s)
            val q = roadNetTables.roadNodes.filter(_.geom @&& bbox.dbBBox).map(x => (x.id, x.geom)).sortBy(_._1)
            val newgraph = (SimpleGraph() /: Random.shuffle(q.list).zipWithIndex)(processNode(gr, s))
            val filtered = filterNodes(newgraph, gr)
            filtered.foreach {
                case (start, end, cost) => {
                    val edgQ = for {
                        n1 <- roadNetTables.roadNodes if n1.id === start
                        n2 <- roadNetTables.roadNodes if n2.id === end
                    } yield (n1.id, n2.id, cost, true, n1.geom.shortestLine(n2.geom).asColumnOf[Geometry])
                    roadNetTables.roadNet.insert(edgQ)
                }
            }
        }
    }

    def processNode(dbGraph: DatabaseGraph, s: Session)(sg: SimpleGraph, nd: ((NodeType, Geometry), Int)): SimpleGraph = {
        val ((startid, geom), idx) = nd
        logger.debug(s"Processing node $startid (index = $idx)")
        val union = new UnionGraph(dbGraph, sg)
        val box = {
            val dist = maxDistance
            val point = geom.getInteriorPoint
            val proj = new ApproxEquidistAzimuthProj(point.getX, point.getY)
            val (left, low) = proj.unproject(-dist, -dist)
            val (right, up) = proj.unproject(dist, dist)
            makeBox(makePoint(left, low), makePoint(right, up))
        }

        val dbNodes = {
            val q = for {
                n <- roadNetTables.roadNodes if n.id === startid
                n2 <- roadNetTables.roadNodes if n2.geom @&& box
            } yield n2.id -> getNoRoadCost(n.geom, n2.geom)
            q.list()(s)
        }

        val nodeSet = dbNodes.map(_._1).toSet
        val dijkstraNodes = {
            val dijk = dijkstraForGraph(union)
            dijk.helper.compute(startid).lazyFilter(x => nodeSet.contains(x._1)).take(dbNodes.size).toMap
        }

        val walkingIsFasterNodes = dbNodes.filter {
            case (id, cost) => cost - dijkstraNodes.get(id).getOrElse(Double.PositiveInfinity) < -1E-10 //round off errors
        }

        (sg /: walkingIsFasterNodes) { (grp, edg) =>
            val (end, cost) = edg
            val edges = Seq((startid, end, cost), (end, startid, cost))
            val toAdd = edges.filterNot {
                case (s, e, c) => union.neighbours(s).exists(_._1 == e)
            }
            (grp /: toAdd)((g, e) => (g.withEdge _).tupled(e))
        }
    }

    @tailrec
    private def findWalkingEdges(start: NodeType, sg: SimpleGraph, dbg: DatabaseGraph, dbNodes: Map[NodeType, Double]): SimpleGraph = {
        if (dbNodes.isEmpty)
            sg
        else {
            val union = new UnionGraph(sg, dbg)
            val dijk = dijkstraForGraph(union)
            val (newSg, newDbNodes) = findWalkingEdge(start, sg, union, dijk, dbNodes)
            findWalkingEdges(start, newSg, dbg, newDbNodes)
        }
    }

    private def findWalkingEdge(start: NodeType, sg: SimpleGraph, union: GraphType[NodeType], dijk: DijkstraAlgorithmClass[NodeType], dbNodes: Map[NodeType, Double]): (SimpleGraph, Map[NodeType, Double]) = {
        var currentNodes = dbNodes
        dijk.helper.compute(start).foreach {
            case (nd, cost) => {
                if (currentNodes.contains(nd) && currentNodes(nd) - cost <= -1E-10) {
                    return (addEdge(sg, union, start, nd, currentNodes(nd)), currentNodes - nd)
                }
                currentNodes -= nd
                if (currentNodes.isEmpty)
                    return (sg, currentNodes)
            }
        }
        assert(false)
        return (sg, currentNodes)
    }

    def addEdge(sg: SimpleGraph, union: GraphType[NodeType], start: NodeType, end: NodeType, cost: Double) = {
        val edges = Seq((start, end, cost), (end, start, cost))
        val toAdd = edges.filterNot {
            case (s, e, c) => union.neighbours(s).exists(_._1 == e)
        }
        (sg /: toAdd)((g, e) => (g.withEdge _).tupled(e))
    }
}

trait MaxCostQuotientComponent {
    def maxDistance: Double
}

trait ConfigMaxCostQuotientComponent extends OptionParserComponent with MaxCostQuotientComponent {
    self: ArgumentParser =>

    lazy val maxDistanceLens = registerConfig(50.0)

    lazy val maxDistance = maxDistanceLens.get(parsedConfig)

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) {
        super.parserOptions(pars)
        pars.opt[Double]("max-distance").action((x, c) => maxDistanceLens.set(c)(x))
    }
}