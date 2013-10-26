package org.isochrone.intersections

import org.isochrone.osm.CostAssignerComponent
import org.isochrone.db.DatabaseProvider
import org.isochrone.db.OsmTableComponent
import org.isochrone.db.RoadNetTableComponent
import org.isochrone.db.RegularPartitionComponent
import com.typesafe.scalalogging.slf4j.Logging

trait IncrementalIntersectionRemoverComponent extends IntersectionFinderComponent {
    self: RoadNetTableComponent with OsmTableComponent with DatabaseProvider with CostAssignerComponent with RegularPartitionComponent =>

    object IncrementalIntersectionRemover extends Logging {
        def removeIntersections() {
            for ((reg, i) <- regularPartition.regions.zipWithIndex) {
                logger.info(s"Removing intersections in $reg ($i/${regularPartition.regionCount})")
                IntersectionFinder.removeIntersections(reg.top, reg.left, reg.bottom, reg.right)
            }
        }
    }
}