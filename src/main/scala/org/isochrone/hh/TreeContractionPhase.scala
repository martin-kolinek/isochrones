package org.isochrone.hh

import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.RoadNetTables
import org.isochrone.db.EdgeTable
import org.isochrone.util.db.MyPostgresDriver.simple._
import com.vividsolutions.jts.geom.Geometry
import scala.annotation.tailrec

trait TreeContraction {
    @tailrec
    final def contractTrees(input: RoadNetTables, output: EdgeTable, s: Session): Unit = {
        val leafEdgeQuery = for {
            e <- input.roadNet
            if !(Query(input.roadNet)).filter(x => x.start === e.start && x.end =!= e.end).exists
        } yield e

        val newEdgeQuery = for {
            l <- leafEdgeQuery
            o <- output if o.end === l.start
            n1 <- input.roadNodes if o.start === n1.id
            n2 <- input.roadNodes if l.end === n2.id
        } yield (o.start, l.end, l.cost + o.cost, false, (n1.geom shortestLine n2.geom).asColumnOf[Geometry])

        output.insert(newEdgeQuery)(s)
        val cnt = leafEdgeQuery.delete(s)
        if (cnt > 0)
            contractTrees(input, output, s)
    }
}