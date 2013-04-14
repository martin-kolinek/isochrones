package org.isochrone



object Main extends ActionExecutor with DijkstraIsochroneComputer with Partitioner with HigherLevelCreator {
	def main(args:Array[String]) {
		execute(args.head, args.tail)
	}
}
