package org.isochrone



/*object Main extends ActionExecutor with DijkstraIsochroneComputer with Partitioner with HigherLevelCreator {
	def main(args:Array[String]) {
		execute(args.head, args.tail)
	}
}*/

object Main {
    def createComponent(args:Seq[String]) = {
        new SomeActionComponent with ActionComponent {
            val executor = new Executor {
                def execute() = println("hello")
            }
        }
    }
    
    def main(args:Array[String]) {
        val comp = createComponent(args)
        
        comp.executor.execute()
    }
}