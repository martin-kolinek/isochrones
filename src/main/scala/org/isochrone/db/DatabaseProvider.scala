package org.isochrone.db

import scala.slick.session.Database

trait DatabaseProvider {
	val database:Database
}