package org.isochrone

trait ArgumentParser {
    self: ArgumentsProvider with OptionParserComponent =>

    def parsedConfig = parser.parse(arguments, parserStart).getOrElse {
        System.exit(1)
        throw new Exception("Command line arguments not parsed")
    }
}