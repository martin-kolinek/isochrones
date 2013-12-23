package org.isochrone.osm

import org.isochrone.util.db.MyPostgresDriver.simple._
import com.vividsolutions.jts.geom.Geometry

trait CostAssignerComponent {
    def getRoadCost(c1: Column[Geometry], c2: Column[Geometry]): Column[Double]
    def getNoRoadCost(c1: Column[Geometry], c2: Column[Geometry]): Column[Double]
}

trait SpeedCostAssignerComponent extends CostAssignerComponent {
    def roadSpeed: Double //km/h
    def noRoadSpeed: Double //km/h
    private def cst(c1: Column[Geometry], c2: Column[Geometry], spd: Double) = (c1.distanceSphere(c2).asColumnOf[Double] / 1000.0) / spd
    def getRoadCost(c1: Column[Geometry], c2: Column[Geometry]) = cst(c1, c2, roadSpeed)
    def getNoRoadCost(c1: Column[Geometry], c2: Column[Geometry]) = cst(c1, c2, noRoadSpeed)
}

trait DefaultCostAssignerComponent extends SpeedCostAssignerComponent {
    def roadSpeed = 80
    def noRoadSpeed = 6
}