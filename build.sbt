name := "RareBooksApp"

version := "0.1"

scalaVersion := "2.13.0"

val akkaVersion = "2.5.23"

resolvers += "Lightbend Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion
)