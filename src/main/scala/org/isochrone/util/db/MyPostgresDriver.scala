package org.isochrone.util.db

import scala.slick.driver.PostgresDriver
import com.github.tminglei.slickpg.PgHStoreSupport
import com.github.tminglei.slickpg.PgArraySupport
import com.github.tminglei.slickpg.PostGISSupport

object MyPostgresDriver extends PostgresDriver with PgHStoreSupport with PgArraySupport with PostGISSupport {
	override val Implicit = new Implicits with HStoreImplicits with ArrayImplicits with PostGISImplicits
	
	override val simple = new SimpleQL with HStoreImplicits with ArrayImplicits with PostGISImplicits with PostGISAssistants
}