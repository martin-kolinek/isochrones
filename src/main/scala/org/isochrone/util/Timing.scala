package org.isochrone.util

import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import com.typesafe.scalalogging.slf4j.Logger

object Timing {
    def timed(func: => Unit) = {
        val start = System.currentTimeMillis
        func
        Duration(System.currentTimeMillis - start, TimeUnit.MILLISECONDS)
    }

    def timeLogged(lg: Logger, msg: Duration => String)(f: => Unit) = {
        val tm = timed(f)
        lg.debug(msg(tm))
    }
}