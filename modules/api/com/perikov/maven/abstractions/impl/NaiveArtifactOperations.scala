package com.perikov.maven.abstractions.impl

private case class NaiveArtifact(
    groupId: String,
    artifactId: String,
    version: String,
    packaging: String,
    classifier: String
)

import com.perikov.maven.abstractions.*

given naiveArtifactOperations: ArtifactOperations[NaiveArtifact] with
  override def fromComponents(
      groupId: String,
      artifactId: String,
      version: String,
      packaging: String,
      classifier: String
  ): Artifact =
    NaiveArtifact(groupId, artifactId, version, packaging, classifier)

  extension (s: String)
    override def parseArtifact: Either[String, Artifact] =
      val parts = s.split(":")
      if parts.length == 3 then Right(NaiveArtifact(parts(0), parts(1), parts(2), "jar", ""))
      else Left(s"Invalid artifact path: $s")

  extension (a: Artifact)
    override def pathComponents: Seq[String] =
      a.groupId.split("\\.").toIndexedSeq ++ Seq(
        a.artifactId,
        a.version,
        filename
      )

    override def filename: String =
      import a.*
      s"$artifactId-${a.version}.${packaging}"

    override def bundleSymbolicName: String =
      import a.*
      s"$groupId.$artifactId"

    override def version: String = a.version

    override def pom: Artifact = a.copy(packaging = "pom")
