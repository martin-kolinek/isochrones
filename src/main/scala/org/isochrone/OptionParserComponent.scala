package org.isochrone

import scopt.OptionParser

trait OptionParserComponent {
	type OptionConfig
	
	def parser:OptionParser[OptionConfig]
	
	def parserStart:OptionConfig
}