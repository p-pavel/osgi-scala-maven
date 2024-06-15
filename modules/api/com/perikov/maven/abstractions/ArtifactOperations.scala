package com.perikov.maven.abstractions


/** Simplistic API for parsing and constructing Maven artifact paths
  * @todo
  *   provide proper types to make API suitable as a specification
  */
trait ArtifactOperations[A]:
  type Artifact  = A
  extension (s: String) def parseArtifact: Either[String, Artifact]
  def fromComponents(
      groupId: String,
      artifactId: String,
      version: String,
      packaging: String = "jar",
      classifier: String = ""
  ): Artifact
  extension (a: Artifact)
    def pathComponents: Seq[String]
    def pom: Artifact
end ArtifactOperations

