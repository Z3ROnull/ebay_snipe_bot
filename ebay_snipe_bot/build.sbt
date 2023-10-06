name := "ebay-sniper"

version := "1.0"

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.2.6",
  "com.typesafe.akka" %% "akka-actor" % "2.6.16",
  "io.spray" %% "spray-json" % "1.3.6",
  "net.ruippeixotog" %% "scala-scraper" % "3.0.0",
  "org.slf4j" % "slf4j-log4j12" % "1.7.32",
  "ch.qos.logback" % "logback-classic" % "1.2.6"
  // Add other dependencies as needed
)