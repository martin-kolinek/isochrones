package org.isochrone.output

import org.isochrone.compute.IsochroneOutputComponent
import scalax.io.JavaConverters._
import org.isochrone.ArgumentParser
import scalax.io.Resource
import com.vividsolutions.jts.io.WKTWriter
import com.typesafe.scalalogging.slf4j.Logging

trait GeometryOutputComponent extends OutputOptionsParserComponent with Logging {
    self: IsochroneOutputComponent with ArgumentParser =>

    def output = {
        fileNameLens.get(parsedConfig) match {
            case Some(filename) => Resource.fromFile(filename)
            case None => System.out.asUnmanagedOutput
        }
    }

    val wkt = new WKTWriter
    val newline = sys.props("line.separator")

    def writeOutput() {
        val geom = isochroneGeometry
        logger.info(s"Writing to $output")
        output.writeStrings(Iterable(s"id|geom$newline") ++ geom.toIterable.zipWithIndex.map {
            case (geom, idx) =>
                s"$idx|${wkt.write(geom)}$newline"
        })
    }
}