package org.isochrone.db

import scopt.OptionParser
import org.isochrone.OptionParserComponent

trait OnlyDatabaseParserComponent extends OptionParserComponent with DatabaseOptionParsingComponent {
    type OptionConfig = OnlyDatabaseOption

    case class OnlyDatabaseOption(database: String) extends DatabaseOption {
        def copyWithDb(db: String) = copy(database = db)
    }

    def parser: OptionParser[OptionConfig] = new CommonOptionParser with DatabaseParser {
        databaseOpt
    }

    def parserStart = OnlyDatabaseOption("")
}