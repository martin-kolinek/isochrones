package org.isochrone.dbgraph

import org.isochrone.graphlib.GraphWithRegions
import scala.slick.driver.PostgresDriver.simple._

class DatabaseGraphWithRegions(val g:DatabaseGraph, val reg:RegionTable)(implicit session:Session) extends GraphWithRegions[Long, Int] {
	lazy val regionDiameters = {
		val q = for{
			r<-reg
		} yield r.id -> r.diameter
		q.list.toMap
	}
	
	def neighbours(n:Long) = g.neighbours(n)
	def nodeRegion(n:Long) = g.nodeRegion(n)
	def nodeEccentricity(n:Long) = (for {
		r <- nodeRegion(n)
		d <- regionDiameters.get(r)
	} yield d).getOrElse(Double.PositiveInfinity)
	def nodes = g.nodes
}