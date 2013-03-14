package org.isochrone.util

object Timing {
	def timed(func: =>Unit) = {
		val start = System.currentTimeMillis
		func
		System.currentTimeMillis - start
	}
}