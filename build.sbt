import com.typesafe.sbt.license.{DepModuleInfo, LicenseInfo}

organization := "org.in-cal"

name := "incal-dl4j"

version := "0.3.1-SNAPSHOT"

description := "Convenient wrapper of Deeplearning4J library especially for temporal classification."

isSnapshot := true

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-beta3",
  "org.nd4j" % "nd4j-native-platform" % "1.0.0-beta3",
  "org.slf4j" % "slf4j-api" % "1.7.21",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.in-cal" %% "incal-core" % "0.3.0"
)

// For licenses not automatically downloaded (need to list them manually)
licenseOverrides := {
  case
    DepModuleInfo("org.deeplearning4j", _, _)
  | DepModuleInfo("org.nd4j", _, _)
  | DepModuleInfo("org.bytedeco.javacpp-presets", _, _)
  | DepModuleInfo("org.datavec", "datavec-api", _)
  | DepModuleInfo("org.datavec", "datavec-data-image", _)
  | DepModuleInfo("org.apache.commons", "commons-compress", _)
  | DepModuleInfo("org.apache.commons", "commons-lang3", _)
  | DepModuleInfo("org.apache.commons", "commons-math3", _)
  | DepModuleInfo("commons-codec", "commons-codec", _)
  | DepModuleInfo("commons-io", "commons-io", _)
  | DepModuleInfo("commons-net", "commons-net", _)
  | DepModuleInfo("commons-lang", "commons-lang", _)
  | DepModuleInfo("com.google.guava", "guava", _)
  | DepModuleInfo("com.google.code.gson", "gson", _)
  | DepModuleInfo("org.objenesis", "objenesis", "2.6") =>
    LicenseInfo(LicenseCategory.Apache, "Apache License v2.0", "http://www.apache.org/licenses/LICENSE-2.0")

  // logback libs have a dual LGPL / EPL license, we choose EPL
  case
    DepModuleInfo("ch.qos.logback", "logback-classic", _)
  | DepModuleInfo("ch.qos.logback", "logback-core", _)
  | DepModuleInfo("com.github.oshi", "oshi-core", "3.4.2") // note that oshi-core uses an MIT license from the version 3.13.0
    =>
    LicenseInfo(LicenseCategory.EPL, "Eclipse Public License 1.0", "http://www.eclipse.org/legal/epl-v10.html")

  case
    DepModuleInfo("com.twelvemonkeys.common", _, _)
  | DepModuleInfo("com.twelvemonkeys.imageio", _, _)
  | DepModuleInfo("com.github.os72", "protobuf-java-util-shaded-351", _)
  | DepModuleInfo("com.github.os72", "protobuf-java-shaded-351", _) =>
    LicenseInfo(LicenseCategory.BSD, "BSD-3 Clause", "http://opensource.org/licenses/BSD-3-Clause")
}

// POM settings for Sonatype
homepage := Some(url("https://github.com/in-cal/incal-dl4j"))

publishMavenStyle := true

scmInfo := Some(ScmInfo(url("https://github.com/peterbanda/incal-dl4j"), "scm:git@github.com:peterbanda/incal-dl4j.git"))

developers := List(Developer("bnd", "Peter Banda", "peter.banda@protonmail.com", url("https://peterbanda.net")))

licenses += "Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)
