organization := "org.in-cal"

name := "incal-dl4j"

version := "0.2.1"

description := "Convenient wrapper of Deeplearning4J library especially for temporal classification."

isSnapshot := false

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-beta3",
  "org.nd4j" % "nd4j-native-platform" % "1.0.0-beta3",
  "org.slf4j" % "slf4j-api" % "1.7.21",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.in-cal" %% "incal-core" % "0.2.1"
)

// POM settings for Sonatype
homepage := Some(url("https://in-cal.org"))

publishMavenStyle := true

scmInfo := Some(ScmInfo(url("https://github.com/peterbanda/incal-dl4j"), "scm:git@github.com:peterbanda/incal-dl4j.git"))

developers := List(Developer("bnd", "Peter Banda", "peter.banda@protonmail.com", url("https://peterbanda.net")))

licenses += "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)
