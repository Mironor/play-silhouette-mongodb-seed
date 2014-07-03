import play.PlayScala

scalaVersion := "2.11.1"

name := """play-silhouette-seed"""

version := "1.0"

libraryDependencies ++= Seq(
  "com.novus" %% "salat" % "1.9.8",
  "com.mohiva" %% "play-silhouette" % "1.0",
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "bootstrap" % "3.1.1",
  "org.webjars" % "jquery" % "1.11.0",
  "org.scaldi" %% "scaldi-play" % "0.4",
  cache
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
