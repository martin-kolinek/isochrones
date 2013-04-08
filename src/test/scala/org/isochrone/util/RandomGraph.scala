package org.isochrone.util

import scala.util.Random
import org.isochrone.simplegraph.SimpleGraph

object RandomGraph {
	def randomSymmetricGraph(n:Int, e:Int) = {
		val rand = new Random()
		val rands = Iterator.continually(rand.nextInt(n)).grouped(2).
		    filter{case Seq(a, b) => a!=b}.map(_.toSet).take(e).toSet
		    
		val directed = rands.map(_.toSeq).map{case Seq(a, b) => (a, b)}
		val undirected = directed ++ directed.map(_.swap)
		val weighted = undirected.map(x=>(x._1, x._2, 1.0))
		new SimpleGraph(weighted.toSeq:_*)
	}
}