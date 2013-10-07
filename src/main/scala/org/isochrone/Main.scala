package org.isochrone

import org.isochrone.db.OnlyDatabaseParserComponent
import org.isochrone.db.FromOptionDatabaseComponent
import org.isochrone.osm.TableCreatorComponent
import org.isochrone.db.VisualizationTableComponent
import org.isochrone.db.DefaultVisualizationTableComponent
import org.isochrone.db.DefaultRoadNetTableComponent
import org.isochrone.osm.RoadImporterComponent
import org.isochrone.db.OsmTableComponent
import org.isochrone.db.OnlyDatabaseParserComponent
import org.isochrone.osm.RoadNetVisualizerComponent
import org.isochrone.osm.DefaultCostAssignerComponent

/*object Main extends ActionExecutor with DijkstraIsochroneComputer with Partitioner with HigherLevelCreator {
	def main(args:Array[String]) {
		execute(args.head, args.tail)
	}
}*/

object Main {
    val action = Set("hello")
    
    trait CompleteTableCreatorComponent extends OnlyDatabaseParserComponent with FromOptionDatabaseComponent with TableCreatorComponent with DefaultRoadNetTableComponent with DefaultVisualizationTableComponent {
        self: ArgumentsProvider =>
    }
    
    def createComponent(arguments: Seq[String]) = {
        trait OptionsBase extends ArgumentParser with ArgumentsProvider {
            self: OptionParserComponent =>
            def args = arguments.tail
        }

        arguments.head match {
            case "hello" => new ActionComponent {
                val execute = () => println("hello")
            }
            case "createdb" => new ActionComponent with OptionsBase with CompleteTableCreatorComponent {
                val execute = tableCreator.create _
            }
            case "dropdb" => new ActionComponent with OptionsBase with CompleteTableCreatorComponent {
                val execute = tableCreator.drop _
            }
            case "roadimport" => new ActionComponent with OptionsBase with FromOptionDatabaseComponent with OnlyDatabaseParserComponent with RoadImporterComponent with OsmTableComponent with DefaultRoadNetTableComponent with DefaultCostAssignerComponent {
                val execute = roadImporter.execute _
            } 
            case "roadvisualize" => new ActionComponent with OptionsBase with FromOptionDatabaseComponent with OnlyDatabaseParserComponent with RoadNetVisualizerComponent with DefaultRoadNetTableComponent with DefaultVisualizationTableComponent with OsmTableComponent {
                val execute = visualizer.execute _
            }
        }
    }

    def main(args: Array[String]) {
        val comp = createComponent(args)

        comp.execute()
    }
}