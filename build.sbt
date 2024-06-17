ThisBuild / scalaVersion      := "3.3.1"
ThisBuild / organization      := "com.perikov"
Global / onChangedBuildSource := ReloadOnSourceChanges

val httpSettings = {
  val http4sVersion = "0.23.27"
  libraryDependencies ++= Seq(
    "org.http4s" %% "http4s-ember-client" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-dsl"          % http4sVersion
  )
}

lazy val api = module("api")
  .settings(
    fork                                            := true,
    httpSettings,
    libraryDependencies += "com.datastax.oss"        % "java-driver-core" % "4.14.0",
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml"        % "2.3.0",
    libraryDependencies += "biz.aQute.bnd"           % "biz.aQute.bndlib" % "7.0.0",
    (Compile / console / initialCommands) += "import cats.*, cats.effect.*, org.http4s.client.*, org.http4s.*, com.perikov.maven.abstractions.*",
    scalacOptions ++= Seq("-Ximplicit-search-limit", "1000", "-Wunused:imports"),
    OsgiKeys.privatePackage                         := Seq.empty,
    OsgiKeys.exportPackage                          := Seq(
      "com.perikov.maven.abstractions.*"
    ),
    OsgiKeys.importPackage                          := Seq(
      "*"
    )
  )

lazy val tests = module("tests")
  .dependsOn(api)
  .settings(
    fork              := true,
    libraryDependencies +=
      "org.scalameta" %% "munit" % "0.7.29" 
  )
