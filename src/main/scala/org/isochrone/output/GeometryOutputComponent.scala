package org.isochrone.output

import org.isochrone.compute.IsochroneOutputComponent
import scalax.io.JavaConverters._
import org.isochrone.ArgumentParser
import scalax.io.Resource
import com.vividsolutions.jts.io.WKTWriter

trait GeometryOutputComponent {
    self: IsochroneOutputComponent with ArgumentParser with OutputOptionsParserComponent =>

    def output = {
        fileNameLens.get(parsedConfig) match {
            case Some(filename) => Resource.fromFile(filename)
            case None => System.out.asUnmanagedOutput
        }
    }

    val wkt = new WKTWriter
    val newline = sys.props("line.separator")

    def writeOutput() {
        println(s"writing to $output")
        output.writeStrings(Iterable(s"id|geom$newline") ++ isochroneGeometry.toIterable.zipWithIndex.map {
            case (geom, idx) =>
                s"$idx|${wkt.write(geom)}$newline"
        })
    }
}