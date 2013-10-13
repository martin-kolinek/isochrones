package org.isochrone

import scopt.OptionParser
import shapeless.Lens
import scopt.OptionParser
import scopt.OptionParser

trait OptionParserComponent {
    class OptionConfig private[OptionParserComponent] (private[OptionParserComponent] val m: Map[Int, Any]) {
    }

    final def parserStart = new OptionConfig(Map[Int, Any]())

    private var key = 0: Int

    protected final def registerConfig[T](init: T) = {
        key += 1
        val k = key
        new Lens[OptionConfig, T] {
            def get(c: OptionConfig) = {
                if (c.m.contains(k))
                    c.m(k).asInstanceOf[T]
                else
                    init
            }
            def set(c: OptionConfig)(v: T) = new OptionConfig(c.m + (k -> v))
        }
    }

    protected def parserOptions(pars: OptionParser[OptionConfig]): Unit = {
        pars.help("help").text("print this usage help")
    }

    final def parser = new OptionParser[OptionConfig]("") {
        parserOptions(this)
    }
}
