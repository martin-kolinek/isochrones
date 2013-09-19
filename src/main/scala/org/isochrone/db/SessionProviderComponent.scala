package org.isochrone.db

import scala.slick.session.Session

trait SessionProviderComponent {
	val session:Session
	
	def close():Unit
}

trait SingleSessionProvider extends SessionProviderComponent {
    self:DatabaseProvider =>
    
    lazy val session = database.createSession
    
    def close() = session.close()
}