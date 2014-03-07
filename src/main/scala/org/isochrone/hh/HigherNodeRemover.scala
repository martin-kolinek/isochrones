package org.isochrone.hh

import org.isochrone.db.DatabaseProvider
import org.isochrone.db.RoadNetTables
import org.isochrone.util.db.MyPostgresDriver.simple._
import com.typesafe.scalalogging.slf4j.Logging

trait HigherNodeRemoverComponent {
    self: DatabaseProvider =>
    object HigherNodeRemover extends Logging {
        def removeHigherNodes(rnet: RoadNetTables) {
            database.withTransaction { implicit s =>
                val deleted = rnet.roadNodes.filter { nd =>
                    !rnet.roadNet.filter(net => net.start === nd.id).exists
                }.filter { nd =>
                    !rnet.roadNet.filter(net => net.end === nd.id).exists
                }.delete
                logger.info(s"Deleted $deleted nodes")
            }
        }
    }
}