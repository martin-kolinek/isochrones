package org.isochrone.util.db

import scala.slick.driver.PostgresDriver
import com.github.tminglei.slickpg.PgHStoreSupport
import com.github.tminglei.slickpg.PgArraySupport
import com.github.tminglei.slickpg.PgPostGISSupport
import scala.slick.ast.Library.SqlFunction
import scala.slick.lifted._
import com.vividsolutions.jts.geom.Geometry
import scala.language.implicitConversions

object MyPostgresDriver extends PostgresDriver with PgHStoreSupport with PgArraySupport with PgPostGISSupport {
    class MyGeometryColumnExtensionMethods[G1 <: GEOMETRY, P1](val c: Column[P1])(
        implicit tm: JdbcType[GEOMETRY], tm1: JdbcType[POINT], tm2: JdbcType[LINESTRING], tm3: JdbcType[POLYGON], tm4: JdbcType[GEOMETRYCOLLECTION])
            extends ExtensionMethods[G1, P1] {
        def expand[P2, R](radius: Column[Float])(implicit om: o#to[GEOMETRY, R]) = {
            om.column(ExpandFunc.Expand, n, radius.toNode)
        }
    }

    trait MyImplicits extends PostGISImplicits {
        implicit def myGeomExts[G <: Geometry](c: Column[G]) = new MyGeometryColumnExtensionMethods[G, G](c)
    }

    trait ImplicitsPlus extends Implicits with HStoreImplicits with ArrayImplicits with MyImplicits

    override val Implicit = new ImplicitsPlus {}

    override val simple = new SimpleQL with ImplicitsPlus with PostGISAssistants

    object ExpandFunc {
        val Expand = new SqlFunction("ST_Expand")
    }
}