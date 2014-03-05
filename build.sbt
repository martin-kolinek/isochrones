name:="isochrones"

version:="0.1"

scalaVersion:="2.10.3"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

libraryDependencies += "com.typesafe.slick" %% "slick" % "2.0.1-RC1"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.0-M8"

libraryDependencies += "com.jsuereth" %% "scala-arm" % "1.3"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies += "com.github.tminglei" %% "slick-pg" % "0.5.1.2"

libraryDependencies += "com.github.tminglei" %% "slick-pg_jts" % "0.5.1.2"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.1.0"

libraryDependencies ++= List("org.xerial" % "sqlite-jdbc" % "3.7.2"
			    ,"postgresql" % "postgresql" % "9.1-901.jdbc4")
			    
libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2"

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2"

libraryDependencies += "com.chuusai" % "shapeless" % "2.0.0-M1" cross CrossVersion.full

libraryDependencies += "org.spire-math" %% "spire" % "0.6.0"

libraryDependencies ++= List("com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
	"org.slf4j" % "slf4j-api" % "1.7.5",
	"ch.qos.logback" % "logback-classic" % "1.0.13",
	"ch.qos.logback" % "logback-core" % "1.0.13")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

parallelExecution in Test := false

testOptions in Test += Tests.Argument("-oF")

fork := true

connectInput in run := true
