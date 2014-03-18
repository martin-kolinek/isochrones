package org.isochrone.hh

import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.RoadNetTables
import org.isochrone.db.EdgeTable
import org.isochrone.util.db.MyPostgresDriver.simple._
import com.vividsolutions.jts.geom.Geometry
import scala.annotation.tailrec
import com.typesafe.scalalogging.slf4j.Logging

object TreeContraction extends Logging {
    def contractTrees(input: RoadNetTables, output: TableQuery[EdgeTable], revOutput: TableQuery[EdgeTable], s: Session) = {
        @tailrec
        def contractTreesInt(): Unit = {
            logger.info("Tree contraction step")
            val leafEdgeQuery = for {
                e <- input.roadNet
                if !input.roadNet.filter(x => x.start === e.start && x.end =!= e.end).exists
            } yield e

            val backwardsLeafEdgeQuery = for {
                e <- input.roadNet
                if !input.roadNet.filter(x => x.end === e.end && x.start =!= e.start).exists
            } yield e

            val oneWayEdgeQuery = for {
                e <- input.roadNet
                if !input.roadNet.filter(x => x.end === e.start && x.start === e.end).exists
            } yield e

            val newEdgeQuery = for {
                l <- leafEdgeQuery
                o <- output if o.end === l.start
                n1 <- input.roadNodes if o.start === n1.id
                n2 <- input.roadNodes if l.end === n2.id
            } yield (o.start, l.end, l.cost + o.cost, false, (n1.geom shortestLine n2.geom).asColumnOf[Geometry])

            val revNewEdgeQuery = for {
                l <- backwardsLeafEdgeQuery
                o <- revOutput if o.start === l.end
                n1 <- input.roadNodes if l.start === n1.id
                n2 <- input.roadNodes if o.end === n2.id
            } yield (l.start, o.end, l.cost + o.cost, false, (n1.geom shortestLine n2.geom).asColumnOf[Geometry])

            val inserted = output.insert(leafEdgeQuery)(s) + output.insert(newEdgeQuery)(s)
            val revInserted = revOutput.insert(backwardsLeafEdgeQuery)(s) + revOutput.insert(revNewEdgeQuery)(s)

            assert(inserted == revInserted)

            val cnt1 = leafEdgeQuery.delete(s)
            val cnt2 = oneWayEdgeQuery.delete(s)
            val cnt = cnt1 + cnt2
            logger.info(s"Contracted $cnt edges")
            if (cnt > 0)
                contractTreesInt()
        }

        contractTreesInt()
        logger.info("Removing isolated nodes")
        val isolatedNodes = for {
            n <- input.roadNodes
            if !input.roadNet.filter(_.start === n.id).exists
        } yield n.id

        def toDel(out: TableQuery[EdgeTable]) = out.filter(o => o.start.in(isolatedNodes) && o.end.in(isolatedNodes))
        toDel(output).delete(s)
        toDel(revOutput).delete(s)
    }
}