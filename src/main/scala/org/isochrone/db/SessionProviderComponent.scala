package org.isochrone.db

import scala.slick.session.Session

trait SessionProviderComponent {
	val session:Session
}

trait SingleSessionProvider extends SessionProviderComponent {
    self:DatabaseProvider =>
    
    lazy val session = database.createSession
}