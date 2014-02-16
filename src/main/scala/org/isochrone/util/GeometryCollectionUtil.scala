package org.isochrone.util

import com.vividsolutions.jts.geom.Geometry

trait GeometryCollectionUtil {
    implicit class GeomColOps(g: Geometry) {
        def individualGeometries = {
            (0 until g.getNumGeometries).map(g.getGeometryN)
        }
    }
}