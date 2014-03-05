package org.isochrone.util.db

import scala.slick.driver.PostgresDriver
import com.github.tminglei.slickpg.PgHStoreSupport
import com.github.tminglei.slickpg.PgArraySupport
import com.github.tminglei.slickpg.PgPostGISSupport

object MyPostgresDriver extends PostgresDriver with PgHStoreSupport with PgArraySupport with PgPostGISSupport {
    trait ImplicitsPlus extends Implicits with HStoreImplicits with ArrayImplicits with PostGISImplicits

    override val Implicit = new ImplicitsPlus {}

    override val simple = new SimpleQL with ImplicitsPlus with PostGISAssistants
}