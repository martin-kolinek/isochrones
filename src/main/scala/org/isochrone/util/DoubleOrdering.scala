package org.isochrone.util

import scala.math._

case class DoublePrecision(d:Double){}

object DoubleWithPrecision {
  implicit def doubleOrdering(implicit prec:DoublePrecision) = new Ordering[Double]{
      def compare(x:Double, y:Double) = if(abs(x-y)<prec.d) 0 else if(x<y) -1 else 1
    }
}
