package org.isochrone.util

import scala.util.Random
import org.isochrone.simplegraph.SimpleGraph

object RandomGraph {
	def randomGraph(n:Int, e:Int) = {
		val rand = new Random()
		val rands = Iterator.continually(rand.nextInt(n)).grouped(2).
		    filter{case Seq(a, b) => a!=b}.take(e).toSet
		    
		new SimpleGraph(rands.map{
			case Seq(a, b) => (a, b, 1.0)
		}.toSeq:_*)
	}
}