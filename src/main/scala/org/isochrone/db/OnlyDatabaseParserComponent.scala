package org.isochrone.db

import scopt.OptionParser

trait OnlyDatabaseParserComponent extends DatabaseOptionParsingComponent {
    type OptionConfig = OnlyDatabaseOption
    
    case class OnlyDatabaseOption(database:String) extends DatabaseOption {
        def copyWithDb(db:String) = copy(database=db)
    } 
    
    def parser:OptionParser[OptionConfig] = new OptionParser[OptionConfig]("jhkjh") with DatabaseParser {
    	databaseOpt
    }
    
    def parserStart = OnlyDatabaseOption("") 
}