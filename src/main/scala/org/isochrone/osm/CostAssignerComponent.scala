package org.isochrone.osm

import org.isochrone.util.db.MyPostgresDriver.simple._
import com.vividsolutions.jts.geom.Geometry

trait CostAssignerComponent {
    def getRoadCost(c1: Column[Geometry], c2: Column[Geometry]): Column[Double]
    def getNoRoadCost(c1: Column[Geometry], c2: Column[Geometry]): Column[Double]
}

trait DefaultCostAssignerComponent extends CostAssignerComponent {
    //80 km/h
    def getRoadCost(c1: Column[Geometry], c2: Column[Geometry]) = c1.distanceSphere(c2).asColumnOf[Double] / 1000.0 / 80.0
    //6 km/h
    def getNoRoadCost(c1: Column[Geometry], c2: Column[Geometry]) = c1.distanceSphere(c2).asColumnOf[Double] / 6000.0
}