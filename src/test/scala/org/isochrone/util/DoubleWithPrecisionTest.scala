package org.isochrone.util

import org.scalatest.FunSuite
import DoubleWithPrecision._
import Ordering.Implicits._

class DoubleWithPrecisionTest extends FunSuite {
  test("implicit ordering works") {
    implicit val prec = DoublePrecision(0.1)
    testComp(0.0, 1.0)
    testNotComp(0.0, 0.00001)
  }

  def testComp[T:Ordering](x:T, y:T) {    
    assert(x<y)
  }

  def testNotComp[T:Ordering](x:T, y:T) {
    assert(!(x<y))
  }

  test("implicit tuple compare") {
    implicit val prec = DoublePrecision(0.1)
    testComp((0.0, 1), (1.0, 1))
    testNotComp((0.0, 1), (0.001, 1))
  }

}
