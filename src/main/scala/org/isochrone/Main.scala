package org.isochrone



object Main extends ActionExecutor with DijkstraIsochroneComputer {
	def main(args:Array[String]) {
		execute(args.head, args.tail)
	}
}
