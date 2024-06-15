import sbt._
import Keys._
import com.typesafe.sbt.osgi.*

object ProjectPlugin extends AutoPlugin {
  object autoImport {
    def module(nm: String) =
      Project(nm, file(s"modules/$nm"))
        .enablePlugins(SbtOsgi)
        .settings(
          name := nm,
          scalacOptions ++= Seq(
            "-deprecation",
            "-explaintypes",
            "-explain",
            "-rewrite", "-source", "future-migration",
            "-Ykind-projector",
            "-feature",
          ),
          (Compile / scalaSource) := baseDirectory.value ,
        )
  }
}
