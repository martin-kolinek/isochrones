package org.isochrone.visualize

import org.isochrone.util.LRUCache
import com.vividsolutions.jts.geom.Geometry
import org.isochrone.db.SessionProviderComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.util.db.MyPostgresDriver.simple._
import org.isochrone.OptionParserComponent
import scopt.OptionParser
import org.isochrone.ArgumentParser

trait AreaGeometryCacheComponent {

    trait AreaGeometryCache {
        def getAreaGeom(ar: Long): Geometry
    }

    val areaGeomCache: AreaGeometryCache
}

trait DbAreaGeometryCacheComponent extends AreaGeometryCacheComponent {
    self: SessionProviderComponent with RoadNetTableComponent =>
    class DbAreaGeometryCache(maxSize: Int) extends AreaGeometryCache {
        private val cache = new LRUCache[Long, Geometry]((k, v, m) => m.size > maxSize)

        def ensureArea(ar: Long) = {
            if (!cache.contains(ar))
                retrieveArea(ar)
            assert(cache.contains(ar))
        }

        def retrieveArea(ar: Long) = {
            cache(ar) = Query(roadNetTables.areaGeoms).filter(_.id === ar).map(_.geom).first()(session)
        }

        def getAreaGeom(ar: Long) = {
            ensureArea(ar)
            cache(ar)
        }
    }
}

trait ConfigDbAreaGeometryCacheComponent extends DbAreaGeometryCacheComponent with OptionParserComponent {
    self: SessionProviderComponent with RoadNetTableComponent with ArgumentParser =>
    val areaGeomCacheSizeLens = registerConfig(200)

    abstract override def parserOptions(pars: OptionParser[OptionConfig]) = {
        super.parserOptions(pars)
        pars.opt[Int]("area-geom-cache-size").action((x, c) => areaGeomCacheSizeLens.set(c)(x))
    }

    val areaGeomCache = new DbAreaGeometryCache(areaGeomCacheSizeLens.get(parsedConfig))
}
