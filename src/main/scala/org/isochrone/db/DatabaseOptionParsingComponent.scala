package org.isochrone.db

import org.isochrone.OptionParserComponent
import shapeless._
import scopt.OptionParser

trait DatabaseOptionParsingComponent extends OptionParserComponent {

    case class DatabaseOption(db: String)

    val dbLens: Lens[OptionConfig, DatabaseOption] = registerConfig(DatabaseOption(""))

    def defaultDatabaseOption = DatabaseOption("")

    def dbNameLens = (Lens[DatabaseOption] >> 0) compose dbLens

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[String]('d', "database").action((x, c) => dbNameLens.set(c)(x)).text("The database to work with")
    }
}