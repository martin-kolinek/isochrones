package org.isochrone.osm

import org.isochrone.util.db.MyPostgresDriver.simple._
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.Point
import org.isochrone.util._

object Harvesine {

    private val radius = 6370986.0

    def distance(p1: List[Double], p2: List[Double]) = {
        val dlat = math.toRadians(p2.y - p1.y)
        val dlon = math.toRadians(p2.x - p1.x)
        val a = math.sin(dlat / 2.0) * math.sin(dlat / 2.0) +
            math.cos(math.toRadians(p1.y)) * math.cos(math.toRadians(p2.y)) *
            math.sin(dlon / 2.0) * math.sin(dlon / 2.0)
        val c = 2 * math.asin(math.min(1.0, math.sqrt(a)))
        radius * c
    }
}

trait CostAssignerComponent {
    def getRoadCost(c1: Column[Geometry], c2: Column[Geometry]): Column[Double]
    def getNoRoadCost(c1: Column[Geometry], c2: Column[Geometry]): Column[Double]
}

trait SpeedCostAssignerComponent extends CostAssignerComponent {
    def roadSpeed: Double //km/h
    def noRoadSpeed: Double //km/h
    private def cst(c1: Column[Geometry], c2: Column[Geometry], spd: Double) = (c1.distanceSphere(c2).asColumnOf[Double] / 1000.0) / spd
    def metersToRoadCost(meters: Double) = (meters / 1000) / roadSpeed
    def metersToNoRoadCost(meters: Double) = (meters / 1000) / noRoadSpeed
    def roadCostToMeters(cst: Double) = (cst * roadSpeed) * 1000
    def noRoadCostToMeters(cst: Double) = (cst * noRoadSpeed) * 1000
    def getRoadCost(c1: Column[Geometry], c2: Column[Geometry]) = cst(c1, c2, roadSpeed)
    def getNoRoadCost(c1: Column[Geometry], c2: Column[Geometry]) = cst(c1, c2, noRoadSpeed)
    def getNoDbRoadCost(p1: List[Double], p2: List[Double]) = metersToRoadCost(Harvesine.distance(p1, p2))
    def getNoDbNoRoadCost(p1: List[Double], p2: List[Double]) = metersToNoRoadCost(Harvesine.distance(p1, p2))
}

trait DefaultCostAssignerComponent extends SpeedCostAssignerComponent {
    def roadSpeed = 80
    def noRoadSpeed = 6
}