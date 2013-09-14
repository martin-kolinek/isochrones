package org.isochrone

trait ArgumentParser {
	self: ArgumentsProvider with OptionParserComponent =>
	    
	def parsedConfig = parser.parse(args, parserStart)
}