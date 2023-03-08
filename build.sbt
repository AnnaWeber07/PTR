ThisBuild / version := "0.1.0-SNAPSHOT"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.5"
libraryDependencies += "io.spray" %% "spray-json" % "1.3.6"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % "2.7.0",
  "com.typesafe.akka" %% "akka-http" % "10.5.0",
  "com.typesafe.akka" %% "akka-stream" % "2.7.0",
  "com.typesafe.akka" %% "akka-actor" % "2.7.0",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.5.0",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.5.0" % Test,
  "org.scalatest" %% "scalatest" % "3.2.15" % Test
)

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "PTR"
  )
