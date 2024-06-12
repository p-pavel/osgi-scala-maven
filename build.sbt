ThisBuild / scalaVersion := "3.3.1"
ThisBuild / organization := "com.perikov"
Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val api = module("api")
  .settings(
    OsgiKeys.privatePackage := Seq.empty,
    OsgiKeys.exportPackage := Seq(
      "com.perikov.maven.abstractions"
    ),
    OsgiKeys.importPackage := Seq(
      "*"
    ),
  )