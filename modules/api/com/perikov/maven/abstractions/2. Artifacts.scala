package com.perikov.maven.abstractions

/** @example
  *   def t1(a: POM): "pom" = a.packaging
  *
  * @example
  *   def t2(g: GroupId, a: ArtifactId, v:Version): Artifact =
  *       jar.fromComponents((g, a, v, Unknown, "jar"))
  */
trait MavenArtifacts extends MavenCoordinates:

  type Base[S, C, P] = ComponentProduct {
    type Self <: S
    type Components =
      (GroupId, ArtifactId, Version, C, P)
  }

  val artifact: Base[Any, Optional[Classifier], Optional[Packaging]]
  export artifact.Self as Artifact
  given artifact.type = artifact

  val pom: Base[Artifact, Optional[Classifier], "pom"]
  export pom.Self as POM
  given pom.type = pom

  val jar: Base[Artifact, Optional[Classifier], "jar"]
  export jar.Self as JAR
  given jar.type = jar

  val executableJar: Base[JAR, "", "jar"]
  export executableJar.Self as ExecutableJAR
  given executableJar.type = executableJar

  /** @note  overload to avoid constructing components */
  extension [A <: Artifact, C <: artifact.Components](a: A)(using
      evidence: ComponentProductAux[A, C]
  )
    def groupId = a.toComponents(0)
    def artifactId = a.toComponents(1)
    def version = a.toComponents(2)
    def classifier = a.toComponents(3)
    def packaging = a.toComponents(4)
