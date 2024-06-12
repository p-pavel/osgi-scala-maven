package com.perikov.maven.abstractions

/**  Main concepts of Maven coordinates */
trait Maven:
  /** @todo make more precise */
  type GroupId <: String

  /** @todo make more precise */
  type ArtifactId <: String

  type Version

  type Classifier >: KnownClassifiers <: String

  type Packaging >: KnownPackaging <: String

  /** just an utility for `[a] =>> a` to avoid bringing dependency on scala
    * library
    */
  type Id[a] = a

  /** We don't need `Option` since all types we're talking about 
   * are subtypes of `AnyRef`
   */
  type Optional[T] = T | Null

  type Artifact
  type ExecutableJAR <: Artifact
  type RuntimeDependency <: ExecutableJAR

  extension [F[_]](a: Artifact)
    def groupId: GroupId
    def artifactId: ArtifactId
    def version: Version
    def classifier: Optional[Classifier]
    def packaging: Optional[Packaging]

  extension (a: ExecutableJAR)
    inline def packaging: DefaultPackaging =
      compiletime.constValue[DefaultPackaging]
    inline def classifier: DefaultClassifier =
      compiletime.constValue[DefaultClassifier]

  def artifact(
      groupId: GroupId,
      artifactId: ArtifactId,
      version: Version,
      classifier: Optional[Classifier] = compiletime.constValue[DefaultClassifier],
      packaging: Optional[Packaging] = compiletime.constValue[DefaultPackaging]
  ): Artifact


