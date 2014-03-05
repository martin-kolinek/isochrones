package org.isochrone.db

import org.isochrone.util.db.MyPostgresDriver.simple._

trait DatabaseProvider {
	def database:Database
}