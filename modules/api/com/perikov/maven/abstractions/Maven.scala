package com.perikov.maven.abstractions

trait Maven:
  type GroupId
  type ArtifactId
  type Version
  type Classifier
  type Packaging
  type POM

  type Id[a] = a

  type Coordinates[F[_]]
  type Artifact <: Coordinates[Id]
  type ExecutableJAR <: Artifact
  type Dependency <: Artifact

  extension [F[_]](a: Coordinates[F])
    def groupId: F[GroupId]
    def version: F[Version]
    def artifactId: F[ArtifactId]
    def classifier: F[Option[Classifier]]

  type Query <: Coordinates[Flatten]
  type CoordinateConstructor[F[_], Res]
  extension [F[_], Res](a: CoordinateConstructor[F, Res])
    def apply(
        groupId: F[GroupId],
        artifactId: F[ArtifactId],
        version: F[Version],
        classifier: F[Option[Classifier]],
        packaging: F[Option[Packaging]]
    ): Res
  type Flatten[T] =
    T match
      case Option[a] => Option[a]
      case _         => T

  val query: CoordinateConstructor[Flatten, Query]
  val artifact: CoordinateConstructor[Id, Artifact]
