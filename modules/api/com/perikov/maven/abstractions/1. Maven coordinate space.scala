package com.perikov.maven.abstractions

/** We define main concepts of Maven types */
trait Maven:
  /** @todo make more precise */
  type GroupId <: String

  /** @todo make more precise */
  type ArtifactId <: String

  type Version

  type Classifier <: (String | KnonwClassifiers)

  type Packaging <: (String | KnownPackaging)

  type Coordinates <: {
    type groupId
    type artifactId
    type version
    type classifier
    type packaging
  }


  /** just an utility for `[a] =>> a` to avoid bringing dependency on scala
    * library
    */
  type Id[a] = a


  type Artifact <: Coordinates[Id]
  type ExecutableJAR <: Artifact
  type Dependency <: Artifact

  extension [F[_]](a: Coordinates[F])
    def groupId: F[GroupId]
    def version: F[Version]
    def artifactId: F[ArtifactId]
    def classifier: F[Classifier]
    def packaging: F[Packaging]

  extension (a: ExecutableJAR)
    inline def packaging: DefaultPackaging = compiletime.constValue[DefaultPackaging]

  type Optional[T] = T | Null

  /** This is a part of Maven space with some components fixed.
   * @todo probably we can specify that SOME vital components should be fixed
   */
  type Manifold <: Coordinates[Optional]

  def artifact(
      groupId: GroupId,
      artifactId: ArtifactId,
      version: Version,
      classifier: compiletime.constValue[DefaultClassifier],
      packaging: compiletime.constValue[DefaultPackaging]
  ): Artifact

  def query(
      groupId: Optional[GroupId] = null,
      artifactId: Optional[ArtifactId] = null,
      version: Optional[Version] | Null = null,
      classifier: Optional[Classifier] = null,
      packaging: Optional[Packaging] | Null = null
  ): Manifold
