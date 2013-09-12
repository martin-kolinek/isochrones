package org.isochrone

import org.scalatest.FunSuite
import scopt.OptionParser

class ScoptTest extends FunSuite {
    case class Config(str:String)
    
	test("Scopt accepts even if there are more options") {
	    val parser = new OptionParser[Config]("test") {
	        opt[String]('s', "str") action {(x, c) => c.copy(str = x)}
	    }
	    
	    val cfg = parser.parse(Seq("-s", "asdf", "-q", "bsdf"), Config("none"))
	    
	    assert(cfg.isDefined)
	    assert(cfg.get.str == "asdf")
	}
}