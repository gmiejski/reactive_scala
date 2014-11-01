name := "AuctionSystem"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.5",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.5",
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.7")
