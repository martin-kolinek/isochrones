package org.isochrone

import scala.collection.mutable.HashMap

trait ActionExecutor {
	private val actionMap = new HashMap[String, IndexedSeq[String] => Unit]
	
	def registerAction(name:String, act:IndexedSeq[String] => Unit) {
		actionMap(name)=act
	}
	
	def execute(name:String, args:IndexedSeq[String]) {
		actionMap(name)(args)
	}
	
}