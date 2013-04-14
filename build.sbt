name:="isochrones"

version:="0.1"

scalaVersion:="2.10.0"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

libraryDependencies += "com.typesafe.slick" %% "slick" % "1.0.0"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.0-M8"

libraryDependencies += "com.jsuereth" %% "scala-arm" % "1.3"

libraryDependencies ++= List("org.slf4j" % "slf4j-nop" % "1.6.6"
                            ,"org.xerial" % "sqlite-jdbc" % "3.7.2"
			    ,"postgresql" % "postgresql" % "9.1-901.jdbc4")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

