package org.isochrone.db

import scala.slick.session.Database

trait DatabaseProvider {
	def database:Database
}