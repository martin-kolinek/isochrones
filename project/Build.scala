import sbt._
import sbt.Keys._

object MyBuild extends Build {
  lazy val project = Project("root", file(".")) dependsOn(RootProject(uri("git://github.com/martin-kolinek/postgis_slick.git")))
}