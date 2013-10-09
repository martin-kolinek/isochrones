package org.isochrone.db

import org.isochrone.OptionParserComponent
import scopt.OptionParser
import org.isochrone.OptionParserComponentBase

trait DatabaseOptionParsingComponent extends OptionParserComponentBase {

    trait DatabaseOption {
        def copyWithDb(db: String): OptionConfig
        def database: String
    }

    trait DatabaseParser {
        self: OptionParser[OptionConfig] =>

        def databaseOpt = {
            self.opt[String]('d', "database").action((x, c) => c.copyWithDb(x)).text("The database to work with")
        }
    }

    type OptionConfig <: DatabaseOption

}